package com.example.board.event.consumer;

import com.example.board.dto.event.UserUpdatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void handleUserUpdated(String message) {
        try {
            // 메시지 바이트 배열을 객체로 역직렬화
            UserUpdatedEvent event = objectMapper.readValue(message, UserUpdatedEvent.class);

            log.info("[board-service] user-updated: {}", event.toString());

            // 트랜잭션 작업은 비동기 이벤트 호출; 리스너 병목을 피해야 한다
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Error while deserializing message: {}", e.getMessage());
        }
    }

}
