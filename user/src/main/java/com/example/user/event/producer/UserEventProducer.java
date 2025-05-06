package com.example.user.event.producer;

import com.example.user.domain.entity.OutboxEvent;
import com.example.user.dto.event.UserUpdatedEvent;
import com.example.user.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@EnableKafka
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    // 메시지 보내는 메서드 (비동기)
    public void sendUserUpdatedEvent(String topic, OutboxEvent outbox) { // UserUpdatedEvent message
        kafkaTemplate.send(topic, outbox.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // 아웃박스에 발송 실패 업데이트
                        outbox.markFailed();
                        outboxEventRepository.save(outbox);
                        log.error("[user-service] user-updated 에러 발생 :: {}", ex.getMessage());
                    } else {
                        // 아웃박스에 발송 성공 업데이트
                        outbox.markSent();
                        outboxEventRepository.save(outbox);
                        log.info("[user-service] user-updated 메시지 성공 :: {}", result.getRecordMetadata());
                    }
                });
    }
}
