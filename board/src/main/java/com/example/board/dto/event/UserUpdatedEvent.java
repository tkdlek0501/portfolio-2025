package com.example.board.dto.event;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class UserUpdatedEvent {
    private UUID id;
    private Long userId;
    private String nickname;

    public static UserUpdatedEvent of(UUID id, Long userId, String nickname) {
        return UserUpdatedEvent.builder()
                .id(id)
                .userId(userId)
                .nickname(nickname)
                .build();
    }
}
