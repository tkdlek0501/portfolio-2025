package com.example.board.service;

import com.example.board.domain.entity.Like;
import com.example.board.domain.entity.Post;
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
import com.example.board.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final LikeRepository likeRepository;

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

        // TODO: 게시판 글 작성시 포인트 적립 비동기 이벤트 호출
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

        // TODO: 유저의 정보가 조회에서 필요한 상황
        // 반정규화로 데이터 복제해놓고 데이터가 바뀌면 Kafka 로 갱신 이벤트하는 걸로 처리

        return PostWrapResponse.of(postResponses);
    }

    @Transactional(readOnly = true)
    public PostResponse findById(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        // TODO: 게시글 조회 시 view count 증가 비동기 이벤트 호출

        return PostResponse.of(post.getId(), post.getNickname(), post.getTitle(), post.getContent(), post.getViewCount(), post.getLikeCount());
    }

    @Transactional
    public void createLike(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        long userId = JwtUtil.getId();

        Like like = likeRepository.findByPostIdAndUserId(id, userId)
                .orElse(null);
        if (like != null) throw new NotAllowedLikeException();

        likeRepository.save(Like.create(userId, post.getId()));
        post.increaseLikeCount();
        // TODO: 동시성 제어 - 같은 게시물에 좋아요를 하려 접근 시 처리 필요 -> redis lock or db lock
    }

    @Transactional
    public void deleteLike(long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        long userId = JwtUtil.getId();

        Like like = likeRepository.findByPostIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("like"));

        likeRepository.delete(like);
        post.decreaseLikeCount();
        // TODO: 동시성 제어 - 같은 게시물에 좋아요를 하려 접근 시 처리 필요 -> redis lock or db lock
    }
}
