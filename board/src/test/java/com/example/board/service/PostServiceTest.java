package com.example.board.service;

import com.example.board.domain.entity.Like;
import com.example.board.domain.entity.Post;
import com.example.board.domain.entity.PostCategory;
import com.example.board.dto.event.PostCreatedEvent;
import com.example.board.dto.request.PostCreateRequest;
import com.example.board.dto.request.PostUpdateRequest;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.response.PostWrapResponse;
import com.example.board.repository.LikeRepository;
import com.example.board.repository.PostCategoryRepository;
import com.example.board.repository.PostRepository;
import com.example.board.util.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostCategoryRepository postCategoryRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RLock rLock;

    @DisplayName("게시글 생성 성공")
    @Test
    void create_성공() {
        // given
        Long userId = 1L;
        String nickname = "testUser";
        Long categoryId = 100L;
        PostCreateRequest request = new PostCreateRequest(categoryId, "제목", "내용");

        PostCategory category = mock(PostCategory.class);
        when(postCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        try (MockedStatic<JwtUtil> jwtMock = Mockito.mockStatic(JwtUtil.class)) {
            jwtMock.when(JwtUtil::getId).thenReturn(userId);
            jwtMock.when(JwtUtil::getNickname).thenReturn(nickname);

            // when
            postService.create(request);

            // then
            verify(postRepository).save(any(Post.class));
            verify(eventPublisher).publishEvent(any(PostCreatedEvent.class));
        }
    }

    @DisplayName("게시글 수정 성공")
    @Test
    void update_성공() {
        // given
        Long userId = 1L;
        Long postId = 123L;

        PostUpdateRequest request = new PostUpdateRequest(1L, "수정제목", "수정내용");

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        when(post.getUserId()).thenReturn(userId);

        try (MockedStatic<JwtUtil> jwtMock = Mockito.mockStatic(JwtUtil.class)) {
            jwtMock.when(JwtUtil::getId).thenReturn(userId);

            // when
            postService.update(postId, request);

            // then
            verify(post).modify(anyLong(), any(), any());
        }
    }

    @DisplayName("게시글 삭제 성공")
    @Test
    void delete_성공() {
        Long postId = 123L;
        Long userId = 1L;

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        when(post.getUserId()).thenReturn(userId);

        try (MockedStatic<JwtUtil> jwtMock = Mockito.mockStatic(JwtUtil.class)) {
            jwtMock.when(JwtUtil::getId).thenReturn(userId);

            postService.delete(postId);

            verify(postRepository).deleteById(postId);
        }
    }

    @Test
    @DisplayName("카테고리별 게시글 페이징 조회 성공")
    void findAllByCategoryId_성공() {
        // given
        long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Post post = mock(Post.class);
        when(post.getId()).thenReturn(1L);
        when(post.getNickname()).thenReturn("nickname");
        when(post.getTitle()).thenReturn("title");
        when(post.getContent()).thenReturn("content");
        when(post.getViewCount()).thenReturn(0);
        when(post.getLikeCount()).thenReturn(0);

        Page<Post> postPage = new PageImpl<>(List.of(post), pageable, 1);
        when(postRepository.findAllByPostCategoryId(categoryId, pageable)).thenReturn(postPage);

        // when
        PostWrapResponse result = postService.findAllByCategoryId(categoryId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.posts().getTotalElements());

        PostResponse response = result.posts().getContent().getFirst();
        assertEquals(post.getId(), response.id());
        assertEquals(post.getNickname(), response.nickname());
        assertEquals(post.getTitle(), response.title());
        assertEquals(post.getContent(), response.content());
        assertEquals(post.getViewCount(), response.viewCount());
        assertEquals(post.getLikeCount(), response.likeCount());
    }

    @Test
    @DisplayName("좋아요 생성 성공")
    void createLike_성공() throws Exception {
        Long postId = 1L;
        Long userId = 1L;

//        when(redissonClient.getLock(anyString())).thenReturn(rLock);
//        when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);

        RLock lock = mock(RLock.class);
        when(redissonClient.getLock("board:lock:like:" + postId)).thenReturn(lock);
        when(lock.tryLock(1, 3, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        Post post = mock(Post.class);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(likeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());

        try (MockedStatic<JwtUtil> jwtMock = Mockito.mockStatic(JwtUtil.class)) {
            jwtMock.when(JwtUtil::getId).thenReturn(userId);

            postService.createLike(postId);

            verify(likeRepository).save(any(Like.class));
            verify(post).increaseLikeCount();
        }
    }

    @DisplayName("게시글 좋아요 취소 성공")
    @Test
    void deleteLike_성공() throws InterruptedException {
        // given
        long postId = 1L;
        long userId = 42L;

        // JwtUtil 정적 메서드 mocking
        try (MockedStatic<JwtUtil> mockedJwt = Mockito.mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getId).thenReturn(userId);

            Post post = mock(Post.class);

            Like like = Like.create(userId, postId);

            RLock lock = mock(RLock.class);
            when(redissonClient.getLock("board:lock:like:" + postId)).thenReturn(lock);
            when(lock.tryLock(1, 3, TimeUnit.SECONDS)).thenReturn(true);
            when(lock.isHeldByCurrentThread()).thenReturn(true);

            when(postRepository.findById(postId)).thenReturn(Optional.of(post));
            when(likeRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.of(like));

            // when
            postService.deleteLike(postId);

            // then
            verify(likeRepository).delete(like);
            verify(post).decreaseLikeCount();
            verify(lock).unlock();
        }
    }
}
