package com.example.board.service;

import com.example.board.domain.entity.ChildReply;
import com.example.board.domain.entity.Reply;
import com.example.board.dto.event.ReplyCreatedEvent;
import com.example.board.dto.request.ReplyCreateRequest;
import com.example.board.dto.request.ReplyUpdateRequest;
import com.example.board.dto.response.ReplyResponse;
import com.example.board.exception.ResourceNotFoundException;
import com.example.board.filter.UserContext;
import com.example.board.repository.ChildReplyRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ReplyRepository;
import com.example.board.repository.query.ChildReplyQueryRepository;
import com.example.board.repository.query.ReplyQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ReplyQueryRepository replyQueryRepository;
    private final ChildReplyRepository childReplyRepository;
    private final ChildReplyQueryRepository childReplyQueryRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(long postId, ReplyCreateRequest request) {
        postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("post"));

        boolean isFirst = replyRepository.findByPostIdAndUserId(postId, UserContext.getId()).isEmpty();

        Reply reply = replyRepository.save(Reply.create(UserContext.getId(), postId, UserContext.getNickname(), request.content()));

        if (isFirst) { // 첫 댓글 작성시 포인트 적립
            ReplyCreatedEvent event = ReplyCreatedEvent.of(
                    UUID.randomUUID(),
                    reply.getId(),
                    UserContext.getId(),
                    5
            );
            eventPublisher.publishEvent(event);
        }
    }

    @Transactional
    public void update(long postId, long id, ReplyUpdateRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        Reply reply = replyRepository.findByIdAndUserId(id, UserContext.getId())
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        reply.modify(request.content());
    }

    public void delete(long postId, long id) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        Reply reply = replyRepository.findByIdAndUserId(id, UserContext.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("reply"));

        replyRepository.delete(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getList(Long postId, Long cursor, int size) {
        List<Reply> replies;

        if (cursor == null) {
            // 최초 요청
            replies = replyQueryRepository.findFirstPage(postId, size);
        } else {
            // 더보기 요청
            replies = replyQueryRepository.findNextPage(postId, cursor, size);
        }

        return replies.stream()
                .map(r ->ReplyResponse.of(r.getId(), r.getNickname(), r.getContent(), r.getCreatedDate(), r.getUpdatedDate()))
                .toList();
    }

    public void createChild(long replyId, ReplyCreateRequest request) {
        replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        childReplyRepository.save(ChildReply.create(UserContext.getId(), replyId, UserContext.getNickname(), request.content()));
    }

    @Transactional
    public void updateChild(long replyId, long id, ReplyUpdateRequest request) {
        replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        ChildReply childReply = childReplyRepository.findByIdAndUserId(id, UserContext.getId())
                .orElseThrow(() -> new ResourceNotFoundException("childReply"));

        childReply.modify(request.content());
    }

    public void deleteChild(long replyId, long id) {
        replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        ChildReply childReply = childReplyRepository.findByIdAndUserId(id, UserContext.getId())
                .orElseThrow(() -> new ResourceNotFoundException("childReply"));

        childReplyRepository.delete(childReply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> getChildList(Long postId, Long cursor, int size) {
        List<ChildReply> childReplies;

        if (cursor == null) {
            // 최초 요청
            childReplies = childReplyQueryRepository.findFirstPage(postId, size);
        } else {
            // 더보기 요청
            childReplies = childReplyQueryRepository.findNextPage(postId, cursor, size);
        }

        return childReplies.stream()
                .map(r -> ReplyResponse.of(r.getId(), r.getNickname(), r.getContent(), r.getCreatedDate(), r.getUpdatedDate()))
                .toList();
    }
}
