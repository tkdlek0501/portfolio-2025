package com.example.point.event.listener;

import com.example.point.domain.entity.Point;
import com.example.point.domain.entity.PointHistory;
import com.example.point.dto.event.PostCreatedEvent;
import com.example.point.dto.event.ReplyCreatedEvent;
import com.example.point.repository.PointHistoryRepository;
import com.example.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BoardEventListener {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 게시물 작성에 따른 포인트 지급
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        try {
            // 포인트 지급
            Optional<Point> pointOptional = pointRepository.findByUserId(event.getUserId());
            Point point;
            int preTotalEarnedPoints = 0;
            int preTotalUsedPoints = 0;
            int preAvailablePoints = 0;
            if (pointOptional.isPresent()) {
                point = pointOptional.get();
                preTotalEarnedPoints = point.getTotalEarnedPoints();
                preTotalUsedPoints = point.getTotalUsedPoints();
                preAvailablePoints = point.getAvailablePoints();

                point.update(event.getPoint(), 0);
            } else {
                point = Point.create(event.getUserId(), event.getPoint());
                pointRepository.save(point);
            }

            // 포인트 지급 이력 저장
            PointHistory pointHistory = PointHistory.create(
                    point.getId(), event.getPoint(), 0, preTotalEarnedPoints, preTotalUsedPoints, preAvailablePoints, "게시글 작성 postId : " + event.getPostId());
            pointHistoryRepository.save(pointHistory);
        } catch (Exception e) {
            log.error("[point-service] post-created 처리 실패: {}", e.getMessage(), e);

            // DLQ로 직접 발행
            kafkaTemplate.send("post-created.DLQ", event.getPostId().toString(), event);
        }
    }

    // 댓글 작성에 따른 포인트 지급
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReplyCreatedEvent(ReplyCreatedEvent event) {
        try {
            // 포인트 지급
            Optional<Point> pointOptional = pointRepository.findByUserId(event.getUserId());
            Point point;
            int preTotalEarnedPoints = 0;
            int preTotalUsedPoints = 0;
            int preAvailablePoints = 0;
            if (pointOptional.isPresent()) {
                point = pointOptional.get();
                preTotalEarnedPoints = point.getTotalEarnedPoints();
                preTotalUsedPoints = point.getTotalUsedPoints();
                preAvailablePoints = point.getAvailablePoints();

                point.update(event.getPoint(), 0);
            } else {
                point = Point.create(event.getUserId(), event.getPoint());
                pointRepository.save(point);
            }

            // 포인트 지급 이력 저장
            PointHistory pointHistory = PointHistory.create(
                    point.getId(), event.getPoint(), 0, preTotalEarnedPoints, preTotalUsedPoints, preAvailablePoints, "댓글 작성 replyId : " + event.getReplyId());
            pointHistoryRepository.save(pointHistory);
        } catch (Exception e) {
            log.error("[point-service] reply-created 처리 실패: {}", e.getMessage(), e);

            // DLQ로 직접 발행
            kafkaTemplate.send("reply-created.DLQ", event.getReplyId().toString(), event);
        }
    }
}
