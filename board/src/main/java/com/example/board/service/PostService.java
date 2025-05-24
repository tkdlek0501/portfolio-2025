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
        PostCreatedEvent event = new PostCreatedEvent(
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
        // TODO: 논리적 삭제 하지 않고 물리적 삭제 + 비동기로 삭제된 게시물 테이블 별도 관리하기
        // 연관된 댓글, 대댓글도 처리가 필요할지 고민하기
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
        eventPublisher.publishEvent(new PostViewIncreasedEvent(post.getId(), JwtUtil.getId()));

        return PostResponse.of(post.getId(), post.getNickname(), post.getTitle(), post.getContent(), post.getViewCount(), post.getLikeCount());
    }

    @Transactional
    public void createLike(long id) {
        String lockKey = "board:lock:like:" + id; // 게시물 단위로 동시성 제어
        RLock lock = redissonClient.getLock(lockKey);

        boolean isLocked = false;

        try {
            // 최대 1초 동안 락을 기다리고, 락을 획득한 뒤 최대 3초 유지
            isLocked = lock.tryLock(1, 3, TimeUnit.SECONDS);

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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) { // 현재 스레드가 락을 가지고 있으면
                lock.unlock(); // 락 해제
            }
        }
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
