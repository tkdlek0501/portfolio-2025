package com.example.user.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserUpdatedEvent {
    private Long userId;
    private String nickname;
}
