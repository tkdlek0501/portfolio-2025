package com.example.user.event.listener;

import com.example.user.domain.entity.OutboxEvent;
import com.example.user.dto.event.UserUpdatedEvent;
import com.example.user.event.producer.EventProducer;
import com.example.user.repository.OutboxEventRepository;
import com.example.user.util.kafka.TopicResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventMessageListener {

    private final EventProducer userEventProducer;
    private final TopicResolver topicResolver;
    private final OutboxEventRepository outboxEventRepository;

    // 2. 카프카에 메시지 전송
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 도메인 서비스와 아웃박스 테이블의 커밋 이후를 보장
    public void sendMessageHandler(UserUpdatedEvent event) throws Exception {
        OutboxEvent outbox = outboxEventRepository.findById(event.getId())
                .orElseThrow(Exception::new);

        String topic = topicResolver.resolve(outbox.getAggregateType(), outbox.getEventType());

        userEventProducer.send(topic, outbox);
    }
}
