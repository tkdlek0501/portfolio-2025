package com.example.board.service;

import com.example.board.domain.entity.ChildReply;
import com.example.board.domain.entity.Reply;
import com.example.board.dto.request.ReplyCreateRequest;
import com.example.board.dto.request.ReplyUpdateRequest;
import com.example.board.dto.response.ReplyResponse;
import com.example.board.exception.ResourceNotFoundException;
import com.example.board.repository.ChildReplyRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ReplyRepository;
import com.example.board.repository.query.ChildReplyQueryRepository;
import com.example.board.repository.query.ReplyQueryRepository;
import com.example.board.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final ReplyQueryRepository replyQueryRepository;
    private final ChildReplyRepository childReplyRepository;
    private final ChildReplyQueryRepository childReplyQueryRepository;
    private final PostRepository postRepository;

    public void create(long postId, ReplyCreateRequest request) {
        postRepository.findById(postId)
                        .orElseThrow(() -> new ResourceNotFoundException("post"));

        replyRepository.save(Reply.create(JwtUtil.getId(), postId, JwtUtil.getNickname(), request.content()));
    }

    @Transactional
    public void update(long postId, long id, ReplyUpdateRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        Reply reply = replyRepository.findByIdAndUserId(id, JwtUtil.getId())
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        reply.modify(request.content());
    }

    public void delete(long postId, long id) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post"));

        Reply reply = replyRepository.findByIdAndUserId(id, JwtUtil.getId())
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

        childReplyRepository.save(ChildReply.create(JwtUtil.getId(), replyId, JwtUtil.getNickname(), request.content()));
    }

    @Transactional
    public void updateChild(long replyId, long id, ReplyUpdateRequest request) {
        replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        ChildReply childReply = childReplyRepository.findByIdAndUserId(id, JwtUtil.getId())
                .orElseThrow(() -> new ResourceNotFoundException("childReply"));

        childReply.modify(request.content());
    }

    public void deleteChild(long replyId, long id) {
        replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("reply"));

        ChildReply childReply = childReplyRepository.findByIdAndUserId(id, JwtUtil.getId())
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
                .map(r ->ReplyResponse.of(r.getId(), r.getNickname(), r.getContent(), r.getCreatedDate(), r.getUpdatedDate()))
                .toList();
    }
}
