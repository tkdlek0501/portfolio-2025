package com.example.user.domain.entity;

import com.example.user.domain.enums.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OutboxEventTest {

    @DisplayName("create_성공")
    @Test
    void create_성공() {
        UUID id = UUID.randomUUID();
        String aggregateType = "USER";
        String aggregateId = "user123";
        String eventType = "USER_UPDATED";
        String payload = "{\"key\":\"value\"}";

        OutboxEvent event = OutboxEvent.create(id, aggregateType, aggregateId, eventType, payload);

        assertThat(event).isNotNull();
        assertThat(event.getId()).isEqualTo(id);
        assertThat(event.getAggregateType()).isEqualTo(aggregateType);
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getEventType()).isEqualTo(eventType);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PENDING);
        assertThat(event.isNew()).isTrue();
        assertThat(event.getLastTriedAt()).isNotNull();
    }

    @DisplayName("markSent_성공")
    @Test
    void markSent_성공() {
        OutboxEvent event = OutboxEvent.create(UUID.randomUUID(), "USER", "user123", "USER_UPDATED", "{}");

        event.markSent();

        assertThat(event.getStatus()).isEqualTo(EventStatus.SENT);
        assertThat(event.isNew()).isFalse();
        assertThat(event.getSentAt()).isNotNull();
    }

    @DisplayName("markFailed_성공")
    @Test
    void markFailed_성공() {
        OutboxEvent event = OutboxEvent.create(UUID.randomUUID(), "USER", "user123", "USER_UPDATED", "{}");

        event.markFailed();

        assertThat(event.getStatus()).isEqualTo(EventStatus.FAILED);
        assertThat(event.isNew()).isFalse();
    }

    @DisplayName("increaseRetryCount_성공")
    @Test
    void increaseRetryCount_성공() {
        OutboxEvent event = OutboxEvent.create(UUID.randomUUID(), "USER", "user123", "USER_UPDATED", "{}");
        int before = event.getRetryCount();

        event.increaseRetryCount();

        assertThat(event.getRetryCount()).isEqualTo(before + 1);
    }

    @DisplayName("updateLastTriedAt_성공")
    @Test
    void updateLastTriedAt_성공() throws InterruptedException {
        OutboxEvent event = OutboxEvent.create(UUID.randomUUID(), "USER", "user123", "USER_UPDATED", "{}");
        LocalDateTime before = event.getLastTriedAt();

        Thread.sleep(10); // 약간의 시간 지연

        event.updateLastTriedAt();

        assertThat(event.getLastTriedAt()).isAfter(before);
    }
}
