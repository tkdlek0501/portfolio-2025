package com.example.board.event.listener;

import com.example.board.dto.event.UserUpdatedEvent;
import com.example.board.repository.ChildReplyRepository;
import com.example.board.repository.PostRepository;
import com.example.board.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final ChildReplyRepository childReplyRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleUserUpdatedEvent(UserUpdatedEvent event) {
        try {
            postRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
            replyRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
            childReplyRepository.bulkUpdateNicknameByUserId(event.getUserId(), event.getNickname());
        } catch (Exception e) {
            log.error("[board-service] user-updated 처리 실패: {}", e.getMessage(), e);

            // DLQ로 직접 발행
            kafkaTemplate.send("user-updated.DLQ", event.getUserId().toString(), event);
        }
    }
}
