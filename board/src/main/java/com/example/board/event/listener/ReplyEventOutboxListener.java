package com.example.board.event.listener;

import com.example.board.dto.event.ReplyCreatedEvent;
import com.example.board.helper.factory.OutboxEventFactory;
import com.example.board.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReplyEventOutboxListener {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventFactory outboxEventFactory;

    // 1. 아웃박스 테이블에 이벤트 기록
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT) // 도메인 서비스의 커밋 직전에 호출하여 같은 트랜잭션으로 처리 보장
    public void handleReplyCreatedEvent(ReplyCreatedEvent event) {
        outboxEventRepository.save(outboxEventFactory.create(event.getId(), "REPLY", String.valueOf(event.getReplyId()), "REPLY_CREATED", event));
    }
}
