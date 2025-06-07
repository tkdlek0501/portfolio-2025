package com.example.board.dto.event;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostViewIncreasedEvent {
    private Long postId;
    private Long userId;

    public static PostViewIncreasedEvent of(Long postId, Long userId) {
        return PostViewIncreasedEvent.builder()
                .postId(postId)
                .userId(userId)
                .build();
    }
}
