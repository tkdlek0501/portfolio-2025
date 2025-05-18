package com.example.user.event.producer;

import com.example.user.domain.entity.OutboxEvent;
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
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    // 메시지 보내는 메서드 (비동기)
    public void send(String topic, OutboxEvent outbox) {
        kafkaTemplate.send(topic, outbox.getPayload()) // TODO: 파티션 늘리고 key 명시 - kafkaTemplate.send(topic, key, outbox.getPayload())
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // 아웃박스에 발송 실패 업데이트
                        outbox.markFailed();
                        outboxEventRepository.save(outbox);
                        log.error("[user-service] 메시지 에러 발생 :: ID: {}, Error: {}", outbox.getId(), ex.getMessage());
                    } else {
                        // 아웃박스에 발송 성공 업데이트
                        outbox.markSent();
                        outboxEventRepository.save(outbox);
                        log.info("[user-service] 메시지 성공 :: {}", result.getRecordMetadata());
                    }
                });
    }
}
