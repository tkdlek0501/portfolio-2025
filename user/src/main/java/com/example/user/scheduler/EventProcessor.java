package com.example.user.scheduler;

import com.example.user.domain.entity.OutboxEvent;
import com.example.user.domain.enums.EventStatus;
import com.example.user.event.producer.UserEventProducer;
import com.example.user.repository.OutboxEventRepository;
import com.example.user.util.kafka.TopicResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class EventProcessor { // TODO: 멀티 인스턴스 환경(이중화) 대비 락 처리

    private final OutboxEventRepository outboxEventRepository;
    private final UserEventProducer userEventProducer;
    private final TopicResolver topicResolver;

    // TODO: 실패건 재시도 처리
//    @Scheduled(fixedDelay = 3000)
//    public void processOutbox() {
//        List<OutboxEvent> messages = outboxEventRepository.findTop100ByStatus(EventStatus.PENDING);
//
//        for (OutboxEvent message : messages) {
//            try {
//                // 전송 대상 토픽을 도메인/이벤트 기반으로 결정
//                String topic = topicResolver.resolve(message.getAggregateType(), message.getEventType());
//                userEventProducer.sendUserUpdatedEvent(topic, message);
//            } catch (Exception e) {
//                log.error("Kafka 스케줄러 처리 실패: {}", message.getId(), e);
//            }
//        }
//    }
}
