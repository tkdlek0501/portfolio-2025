package com.example.board.service;

import com.example.board.domain.entity.ChildReply;
import com.example.board.domain.entity.Post;
import com.example.board.domain.entity.Reply;
import com.example.board.dto.event.ReplyCreatedEvent;
import com.example.board.dto.request.ReplyCreateRequest;
import com.example.board.dto.request.ReplyUpdateRequest;
import com.example.board.dto.response.ReplyResponse;
import com.example.board.filter.UserContext;
import com.example.board.repository.ChildReplyRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ReplyRepository;
import com.example.board.repository.query.ChildReplyQueryRepository;
import com.example.board.repository.query.ReplyQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReplyServiceTest {

    @InjectMocks
    private ReplyService replyService;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private ReplyQueryRepository replyQueryRepository;

    @Mock
    private ChildReplyRepository childReplyRepository;

    @Mock
    private ChildReplyQueryRepository childReplyQueryRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @DisplayName("댓글 생성 성공")
    @Test
    void create_성공() {
        // given
        long postId = 1L;
        long userId = 100L;
        String nickname = "testUser";
        String content = "댓글 내용";

        ReplyCreateRequest request = new ReplyCreateRequest(content);

        try (MockedStatic<UserContext> mockedJwt = Mockito.mockStatic(UserContext.class)) {
            mockedJwt.when(UserContext::getId).thenReturn(userId);
            mockedJwt.when(UserContext::getNickname).thenReturn(nickname);

            when(postRepository.findById(postId)).thenReturn(Optional.of(mock()));
            when(replyRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
            when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            replyService.create(postId, request);

            // then
            verify(replyRepository).save(any(Reply.class));
            verify(eventPublisher).publishEvent(any(ReplyCreatedEvent.class));
        }
    }

    @DisplayName("댓글 생성 시 첫 댓글 아닌경우 이벤트 발행 되지않음")
    @Test
    void create_첫댓글아닌경우_이벤트_발행되지않음() {
        long postId = 1L;
        long userId = 101L;
        String nickname = "user";
        String content = "다시 댓글";

        ReplyCreateRequest request = new ReplyCreateRequest(content);

        try (MockedStatic<UserContext> mockedJwt = Mockito.mockStatic(UserContext.class)) {
            mockedJwt.when(UserContext::getId).thenReturn(userId);
            mockedJwt.when(UserContext::getNickname).thenReturn(nickname);

            when(postRepository.findById(postId)).thenReturn(Optional.of(mock()));
            when(replyRepository.findByPostIdAndUserId(postId, userId)).thenReturn(Optional.empty());
            when(replyRepository.save(any(Reply.class))).thenAnswer(invocation -> invocation.getArgument(0));

            replyService.create(postId, request);

            verify(replyRepository).save(any(Reply.class));
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @DisplayName("댓글 수정 성공")
    @Test
    void update_성공() {
        // given
        long postId = 1L, replyId = 2L;
        long userId = 101L;
        ReplyUpdateRequest request = new ReplyUpdateRequest("updated");
        Reply reply = mock(Reply.class);

        try (MockedStatic<UserContext> mockedJwt = Mockito.mockStatic(UserContext.class)) {
            mockedJwt.when(UserContext::getId).thenReturn(userId);

            given(postRepository.findById(postId)).willReturn(Optional.of(mock(Post.class)));
            given(replyRepository.findByIdAndUserId(replyId, userId)).willReturn(Optional.of(reply));

            // when
            replyService.update(postId, replyId, request);

            // then
            verify(reply).modify("updated");
        }
    }

    @DisplayName("댓글 삭제 성공")
    @Test
    void delete_성공() {
        // given
        long postId = 1L, replyId = 2L;
        long userId = 101L;
        Reply reply = mock(Reply.class);

        try (MockedStatic<UserContext> mockedJwt = Mockito.mockStatic(UserContext.class)) {
            mockedJwt.when(UserContext::getId).thenReturn(userId);
            given(postRepository.findById(postId)).willReturn(Optional.of(mock(Post.class)));
            given(replyRepository.findByIdAndUserId(replyId, userId)).willReturn(Optional.of(reply));

            // when
            replyService.delete(postId, replyId);

            // then
            verify(replyRepository).delete(reply);
        }
    }

    @DisplayName("댓글 리스트 조회 성공")
    @Test
    void getList_성공() {
        // given
        long postId = 1L;

        Reply reply = mock(Reply.class);
        when(reply.getId()).thenReturn(1L);
        when(reply.getContent()).thenReturn("content1");

        List<Reply> mockReplies = List.of(
                reply
        );

        given(replyQueryRepository.findFirstPage(postId, 10)).willReturn(mockReplies);

        // when
        List<ReplyResponse> responses = replyService.getList(postId, null, 10);

        // then
        assertEquals(1, responses.size());
        assertEquals("content1", responses.getFirst().content());
    }

    @DisplayName("대댓글 생성 성공")
    @Test
    void createChild_성공() {
        // given
        long replyId = 1L;
        long userId = 101L;
        String nickname = "testUser";
        ReplyCreateRequest request = new ReplyCreateRequest("child content");

        given(replyRepository.findById(replyId)).willReturn(Optional.of(mock(Reply.class)));

        // when
        try (MockedStatic<UserContext> mockedJwt = Mockito.mockStatic(UserContext.class)) {
            mockedJwt.when(UserContext::getId).thenReturn(userId);
            mockedJwt.when(UserContext::getNickname).thenReturn(nickname);
            replyService.createChild(replyId, request);

            // then
            verify(childReplyRepository).save(any(ChildReply.class));
        }
    }
}
