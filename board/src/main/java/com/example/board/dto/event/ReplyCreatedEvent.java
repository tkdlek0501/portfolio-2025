package com.example.board.dto.event;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyCreatedEvent {
    private UUID id;
    private Long replyId;
    private Long userId;
    private int point;

    public static ReplyCreatedEvent of(UUID id, Long replyId, Long userId, int point) {
        return ReplyCreatedEvent.builder()
                .id(id)
                .replyId(replyId)
                .userId(userId)
                .point(point)
                .build();
    }
}
