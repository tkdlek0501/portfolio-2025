package com.example.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "points")
public class Point extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_earned_points", nullable = false)
    private int totalEarnedPoints; // 총 적립 포인트

    @Column(name = "total_used_points", nullable = false)
    private int totalUsedPoints; // 사용 포인트

    @Column(name = "available_points", nullable = false)
    private int availablePoints; // 사용 가능 포인트

    @Column(name = "created_date", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedDate;

    public static Point create(
            Long userId,
            int earnedPoints
    ) {
        return Point.builder()
                .userId(userId)
                .totalEarnedPoints(earnedPoints)
                .totalUsedPoints(0)
                .availablePoints(earnedPoints)
                .build();
    }

    public void update(
            int earnedPoints,
            int usedPoints
    ) {
        this.totalEarnedPoints += earnedPoints;
        this.totalUsedPoints -= usedPoints;
        this.availablePoints += (earnedPoints - usedPoints);
    }
}
