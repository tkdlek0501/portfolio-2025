package com.example.board.service;

import com.example.board.domain.entity.Post;
import com.example.board.dto.request.PostCreateRequest;
import com.example.board.dto.request.PostUpdateRequest;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.response.PostWrapResponse;
import com.example.board.exception.NotAllowedPostException;
import com.example.board.exception.ResourceNotFoundException;
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

    public void create(PostCreateRequest request) {
        Post post = Post.create(
                JwtUtil.getId(),
                request.postCategoryId(),
                request.title(),
                request.content()
        );
        postRepository.save(post);
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
    }

    @Transactional(readOnly = true)
    public PostWrapResponse findAllByCategoryId(long categoryId, Pageable pageable) {
        Page<Post> posts = postRepository.findAllByPostCategoryId(categoryId, pageable);

        Page<PostResponse> postResponses = posts
                .map(p -> PostResponse.of(p.getId(), "", p.getTitle(), p.getContent(), p.getViewCount(), p.getLikeCount()));

        // TODO: 유저의 정보가 조회에서 필요한 상황
        // 동기 API 호출해서 가져와야 한다 (Feign, WebClient 등)
        // Kafka 는 실시간 알림 or 데이터 변경 이벤트 같은 비동기 상황에 쓴다

        // 두 가지 방식이 있다
        // 1. 동기 API 로 처리
        // 2. 반정규화로 데이터 복제해놓고 데이터가 바뀌면 Kafka 로 갱신 이벤트하는 걸로 처리
        // => Event-Carried State Transfer

        return PostWrapResponse.of(postResponses);
    }
}
