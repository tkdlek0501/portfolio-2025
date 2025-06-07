package com.example.user.dto.event;

import lombok.*;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
