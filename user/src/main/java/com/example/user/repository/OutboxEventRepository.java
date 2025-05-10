package com.example.user.repository;

import com.example.user.domain.entity.OutboxEvent;
import com.example.user.domain.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusAndRetryCountLessThanAndLastTriedAtBefore(EventStatus status, int retryCount, LocalDateTime datetime);
}
