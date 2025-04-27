package com.example.user.event.producer;

import com.example.user.dto.event.UserUpdatedEvent;
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

    private static final String UPDATED_TOPIC = "user-updated";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 메시지 보내는 메서드 (비동기)
    public void sendUserUpdatedEvent(UserUpdatedEvent message) {
        kafkaTemplate.send(UPDATED_TOPIC, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        // TODO: 재시도 처리
//                        handleFailure(data, record, ex);
                        log.error("[user-service] user-updated 에러 발생 :: {}", ex.getMessage());
                    } else {
                        // TODO: 아웃박스 패턴
//                        handleSuccess(data);
                        log.info("[user-service] user-updated 메시지 성공 :: {}", result.getRecordMetadata());
                    }
                });
    }
}
