package com.example.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "point_histories")
public class PointHistory extends BaseEntity {

    @Column(name = "point_id", nullable = false)
    private Long pointId; // 포인트 ID

    @Column(name = "earned_points", nullable = false)
    private int earnedPoints; // 적립 포인트

    @Column(name = "used_points", nullable = false)
    private int usedPoints; // 사용 포인트

    @Column(name = "pre_total_earned_points", nullable = false)
    private int preTotalEarnedPoints; // 이전 총 적립 포인트

    @Column(name = "pre_total_used_points", nullable = false)
    private int preTotalUsedPoints; // 이전 총 사용 포인트

    @Column(name = "pre_available_points", nullable = false)
    private int preAvailablePoints; // 이전 사용 가능 포인트

    @Column(name = "reason", nullable = false)
    private String reason; // 포인트 적립/사용 사유

    @Column(name = "created_date", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate; // 생성일

    public static PointHistory create(
            Long pointId,
            int earnedPoints,
            int usedPoints,
            int preTotalEarnedPoints,
            int preTotalUsedPoints,
            int preAvailablePoints,
            String reason
    ) {
        return PointHistory.builder()
                .pointId(pointId)
                .earnedPoints(earnedPoints)
                .usedPoints(usedPoints)
                .preTotalEarnedPoints(preTotalEarnedPoints)
                .preTotalUsedPoints(preTotalUsedPoints)
                .preAvailablePoints(preAvailablePoints)
                .reason(reason)
                .build();
    }
}
