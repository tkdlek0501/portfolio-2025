package com.example.point.dto.event;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCreatedEvent {
    private UUID id;
    private Long postId;
    private Long userId;
    private int point;

    public static PostCreatedEvent of(UUID id, Long postId, Long userId, int point) {
        return PostCreatedEvent.builder()
                .id(id)
                .postId(postId)
                .userId(userId)
                .point(point)
                .build();
    }
}
