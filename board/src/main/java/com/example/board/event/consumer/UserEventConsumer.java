package com.example.board.event.consumer;

import com.example.board.dto.event.UserUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;

    @KafkaListener(topics = "user-updated", groupId = "board-service")
    public void consume(String message) {
        try {
            UserUpdatedEvent event = objectMapper.readValue(message, UserUpdatedEvent.class);

            log.info("[board-service] user-updated: {}", event);

            // 트랜잭션 작업은 비동기 이벤트 호출; 리스너 병목을 피해야 한다
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("[board-service] user-updated 소비 실패 : {}", e.getMessage());
            throw new RuntimeException(e); // 예외 발생시 3회 재시도, 이후 DLQ
        }
    }

    @KafkaListener(topics = "user-updated.DLQ", groupId = "dlq-monitor-group")
    public void consumeDlq(ConsumerRecord<String, String> record) {
        log.error("[board-service] user-updated dlq");
        log.error("Topic: {}", record.topic());
        log.error("Partition: {}", record.partition());
        log.error("Key: {}", record.key());
        log.error("Value: {}", record.value());
        log.error("Offset: {}", record.offset());

        // 필요하면 슬랙 등 알림 추가
    }
}
