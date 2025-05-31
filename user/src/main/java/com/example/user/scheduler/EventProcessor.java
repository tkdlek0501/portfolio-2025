package com.example.user.scheduler;

import com.example.user.domain.entity.OutboxEvent;
import com.example.user.domain.enums.EventStatus;
import com.example.user.repository.OutboxEventRepository;
import com.example.user.util.kafka.TopicResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class EventProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TopicResolver topicResolver;
    private final RedissonClient redissonClient;

    private static final int MAX_RETRY = 5;
    private static final String LOCK_NAME = "user:outbox-processor-lock";

    // 카프카 메시지 발행 실패건 재시도 처리
    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        RLock lock = redissonClient.getLock(LOCK_NAME);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!acquired) {
                log.info("[user-service] 락 획득 실패. 다른 인스턴스가 작업 중입니다.");
                return;
            }
            long lockStartTime = System.currentTimeMillis();
            long lockLeaseTimeMs = 30_000; // 락 만료 시간 30초

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
                    log.info("[user-service] 메시지 재전송 성공 - ID: {}, offset: {}",
                            event.getId(), result.getRecordMetadata().offset());

                } catch (Exception e) {
                    event.increaseRetryCount();
                    event.markFailed();
                    log.warn("[user-service] 메시지 재전송 실패 - ID: {}, Error: {}", event.getId(), e.getMessage());
                }

                event.updateLastTriedAt();
                outboxEventRepository.save(event);

                long elapsed = System.currentTimeMillis() - lockStartTime;
                long remaining = lockLeaseTimeMs - elapsed;
                if (remaining < 3000) { // 3초 이하 남았으면 경고 로그
                    log.warn("[board-service] processOutbox 락 만료 임박: 작업이 락 만료 전에 완료되지 않을 가능성이 있습니다. " +
                            "경과시간={}ms, 남은시간={}ms", elapsed, remaining);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[board-service] 스케줄러 락 획득 중 인터럽트 발생", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
