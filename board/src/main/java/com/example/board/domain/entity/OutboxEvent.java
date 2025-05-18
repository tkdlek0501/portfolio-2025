package com.example.board.domain.entity;

import com.example.board.domain.enums.EventStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "outbox_event")
public class OutboxEvent implements Persistable<UUID>, Serializable {

    @Id
    private UUID id; // 추후 데이터 복제나 CDC 방식 등을 위한 확장성 고려

    private String aggregateType; // ex: "USER"
    private String aggregateId;   // ex: 유저 ID
    private String eventType;     // ex: "USER_UPDATED"

    @Lob
    private String payload;       // JSON 직렬화된 이벤트

    @Enumerated(EnumType.STRING)
    private EventStatus status;   // PENDING, SENT, FAILED

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private int retryCount; // 실패건 재시도 횟수

    private LocalDateTime lastTriedAt; // 마지막 시도 시간

    @Transient
    @JsonIgnore
    private boolean isNew = true;

    public static OutboxEvent create(UUID id, String aggregateType, String aggregateId, String eventType, String json) {
            return OutboxEvent.builder()
                    .id(id)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .status(EventStatus.PENDING)
                    .lastTriedAt(LocalDateTime.now())
                    .isNew(true)
                    .build();
    }

    public void markSent() {
        this.isNew = false;
        this.status = EventStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.isNew = false;
        this.status = EventStatus.FAILED;
    }

    public void increaseRetryCount() {
        this.retryCount++;
    }

    public void updateLastTriedAt() {
        this.lastTriedAt = LocalDateTime.now();
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}
