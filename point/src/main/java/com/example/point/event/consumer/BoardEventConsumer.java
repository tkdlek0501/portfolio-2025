package com.example.point.event.consumer;

import com.example.point.dto.event.PostCreatedEvent;
import com.example.point.dto.event.ReplyCreatedEvent;
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
public class BoardEventConsumer {

    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;

    @KafkaListener(topics = "post-created", groupId = "point-service")
    public void consumePostCreated(String message) {
        try {
            PostCreatedEvent event = objectMapper.readValue(message, PostCreatedEvent.class);

            log.info("[point-service] post-created: {}", event);

            // 트랜잭션 작업은 비동기 이벤트 호출; 리스너 병목을 피해야 한다
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("[point-service] post-created 소비 실패 : {}", e.getMessage());
            throw new RuntimeException(e); // 예외 발생시 3회 재시도, 이후 DLQ
        }
    }

    @KafkaListener(topics = "post-created.DLQ", groupId = "dlq-monitor-group")
    public void consumePostCreatedDlq(ConsumerRecord<String, String> record) {
        log.error("[point-service] post-created dlq");
        log.error("Topic: {}", record.topic());
        log.error("Partition: {}", record.partition());
        log.error("Key: {}", record.key());
        log.error("Value: {}", record.value());
        log.error("Offset: {}", record.offset());

        // 필요하면 슬랙 등 알림 추가
    }

    @KafkaListener(topics = "reply-created", groupId = "point-service")
    public void consumeReplyCreated(String message) {
        try {
            ReplyCreatedEvent event = objectMapper.readValue(message, ReplyCreatedEvent.class);

            log.info("[point-service] reply-created: {}", event);

            // 트랜잭션 작업은 비동기 이벤트 호출; 리스너 병목을 피해야 한다
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("[point-service] reply-created 소비 실패 : {}", e.getMessage());
            throw new RuntimeException(e); // 예외 발생시 3회 재시도, 이후 DLQ
        }
    }

    @KafkaListener(topics = "reply-created.DLQ", groupId = "dlq-monitor-group")
    public void consumeReplyCreatedDlq(ConsumerRecord<String, String> record) {
        log.error("[point-service] post-created dlq");
        log.error("Topic: {}", record.topic());
        log.error("Partition: {}", record.partition());
        log.error("Key: {}", record.key());
        log.error("Value: {}", record.value());
        log.error("Offset: {}", record.offset());

        // 필요하면 슬랙 등 알림 추가
    }
}
