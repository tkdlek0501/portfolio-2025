package com.example.board.event.listener;

import com.example.board.domain.entity.OutboxEvent;
import com.example.board.dto.event.PostCreatedEvent;
import com.example.board.dto.event.ReplyCreatedEvent;
import com.example.board.event.producer.EventProducer;
import com.example.board.repository.OutboxEventRepository;
import com.example.board.util.kafka.TopicResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReplyEventMessageListener {

    private final EventProducer userEventProducer;
    private final TopicResolver topicResolver;
    private final OutboxEventRepository outboxEventRepository;

    // 2. 카프카에 메시지 전송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 도메인 서비스와 아웃박스 테이블의 커밋 이후를 보장
    public void sendPostCreatedMessageHandler(ReplyCreatedEvent event) throws Exception {
        OutboxEvent outbox = outboxEventRepository.findById(event.getId())
                .orElseThrow(Exception::new);

        String topic = topicResolver.resolve(outbox.getAggregateType(), outbox.getEventType());

        userEventProducer.send(topic, outbox);
    }
}
