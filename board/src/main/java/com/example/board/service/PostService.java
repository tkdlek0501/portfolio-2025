package com.example.board.service;

import com.example.board.domain.entity.Like;
import com.example.board.domain.entity.Post;
import com.example.board.dto.event.PostCreatedEvent;
import com.example.board.dto.event.PostViewIncreasedEvent;
import com.example.board.dto.request.PostCreateRequest;
import com.example.board.dto.request.PostUpdateRequest;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.response.PostWrapResponse;
import com.example.board.exception.NotAllowedLikeException;
import com.example.board.exception.NotAllowedPostException;
import com.example.board.exception.ResourceNotFoundException;
import com.example.board.repository.LikeRepository;
import com.example.board.repository.PostCategoryRepository;
import com.example.board.repository.PostRepository;
import com.example.board.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PostService {

    private final RedissonClient redissonClient;
    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(PostCreateRequest request) {
        postCategoryRepository.findById(request.postCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("postCategory"));

        Post post = Post.create(
                JwtUtil.getId(),
                JwtUtil.getNickname(),
                request.postCategoryId(),
                request.title(),
                request.content()
        );
        postRepository.save(post);

        // 게시판 글 작성시 포인트 적립 비동기 이벤트 호출
        PostCreatedEvent event = PostCreatedEvent.of(
                UUID.randomUUID(),
                post.getId(),
                JwtUtil.getId(),
                50
        );
        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void update(long id, PostUpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        if (post.getUserId() != JwtUtil.getId()) {
            throw new NotAllowedPostException();
        }

        post.modify(
                request.postCategoryId(),
                request.title(),
                request.content()
        );
    }

    public void delete(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        if (post.getUserId() != JwtUtil.getId()) {
            throw new NotAllowedPostException();
        }

        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PostWrapResponse findAllByCategoryId(long categoryId, Pageable pageable) {
        Page<Post> posts = postRepository.findAllByPostCategoryId(categoryId, pageable);

        Page<PostResponse> postResponses = posts
                .map(p -> PostResponse.of(p.getId(), p.getNickname(), p.getTitle(), p.getContent(), p.getViewCount(), p.getLikeCount()));

        return PostWrapResponse.of(postResponses);
    }

    @Transactional(readOnly = true)
    public PostResponse findById(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        // 게시글 조회 시 view count 증가 비동기 이벤트 호출
        eventPublisher.publishEvent(PostViewIncreasedEvent.of(post.getId(), JwtUtil.getId()));

        return PostResponse.of(post.getId(), post.getNickname(), post.getTitle(), post.getContent(), post.getViewCount(), post.getLikeCount());
    }

    // 해당 프로젝트의 시나리오 상 대용량 트래픽을 고려, 이중화된 환경을 가정하여 Redisson 도입
    @Transactional
    public void createLike(long id) {
        String lockKey = "board:lock:like:" + id; // 게시물 단위로 동시성 제어
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 최대 1초 동안 락을 기다리고, 락을 획득한 뒤 최대 3초 유지
            boolean isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalStateException("잠시 후 다시 시도해주세요.");
            }

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("post"));

            long userId = JwtUtil.getId();

            Like like = likeRepository.findByPostIdAndUserId(id, userId)
                    .orElse(null);
            if (like != null) throw new NotAllowedLikeException();

            likeRepository.save(Like.create(userId, post.getId()));
            post.increaseLikeCount();

            // 현재 쓰레드에서 트랜잭션 동기화 활성 여부 체크
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                // 트랜잭션에 콜백 등록
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() { // 커밋 이후 바로 호출
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }

                    @Override
                    public void afterCompletion(int status) { // 트랜잭션 종료(롤백 포함) 후 무조건 호출
                        if (status != TransactionSynchronization.STATUS_COMMITTED) { // 커밋 안 된 경우(롤백 등)
                            if (lock.isHeldByCurrentThread()) {
                                lock.unlock();
                            }
                        }
                    }
                });
            } else {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        }
        // 1. 비관적 락 - 확실하게 락을 보장/ 대신 성능 문제 가능
        // 2. Redissson 분산락 - 애플리케이션 레벨에서 락 관리 가능 + 성능 좋음 / 단 커밋 시점의 락을 보장하지는 못함
        //  + 낙관적 락 - 트랜잭션 커밋 시점에 락을 보장하여 db 레벨에서의 동시성 문제 해결 가능
        // 단, version 관리 테이블 추가가 필요하여 고민해볼 사항이 됨
    }

    @Transactional
    public void deleteLike(long id) {
        String lockKey = "board:lock:like:" + id; // 게시물 단위로 동시성 제어
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new IllegalStateException("잠시 후 다시 시도해주세요.");
            }

            Post post = postRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("post"));

            long userId = JwtUtil.getId();

            Like like = likeRepository.findByPostIdAndUserId(id, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("like"));

            likeRepository.delete(like);
            post.decreaseLikeCount();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
