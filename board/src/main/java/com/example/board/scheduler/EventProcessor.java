package com.example.board.scheduler;

import com.example.board.domain.entity.OutboxEvent;
import com.example.board.domain.enums.EventStatus;
import com.example.board.repository.OutboxEventRepository;
import com.example.board.util.kafka.TopicResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class EventProcessor { // TODO: 멀티 인스턴스 환경(이중화) 대비 락 처리

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TopicResolver topicResolver;

    private static final int MAX_RETRY = 5;

    // 카프카 메시지 발행 실패건 재시도 처리
    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        List<OutboxEvent> failedEvents = outboxEventRepository
                .findTop100ByStatusAndRetryCountLessThanAndLastTriedAtBefore(
                        EventStatus.FAILED, MAX_RETRY, LocalDateTime.now().minusSeconds(30)
                );

        for (OutboxEvent event : failedEvents) {
            try {
                String topic = topicResolver.resolve(event.getAggregateType(), event.getEventType());
                // 동기 전송 (결과 보장)
                SendResult<String, Object> result = kafkaTemplate
                        .send(topic, event.getPayload())
                        .get(); // 동기 블록 처리

                event.markSent();
                log.info("[board-service] 메시지 재전송 성공 - ID: {}, offset: {}",
                        event.getId(), result.getRecordMetadata().offset());

            } catch (Exception e) {
                event.increaseRetryCount();
                event.markFailed();
                log.warn("[board-service] 메시지 재전송 실패 - ID: {}, Error: {}", event.getId(), e.getMessage());
            }

            event.updateLastTriedAt();
            outboxEventRepository.save(event);
        }
    }
}
