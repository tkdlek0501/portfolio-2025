package com.example.board.event.listener;

import com.example.board.dto.event.UserUpdatedEvent;
import com.example.board.repository.ChildReplyRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserUpdatedEventListener {

    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final ChildReplyRepository childReplyRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserUpdatedEvent(UserUpdatedEvent event) {

        postRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
        replyRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
        childReplyRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
    }
}
