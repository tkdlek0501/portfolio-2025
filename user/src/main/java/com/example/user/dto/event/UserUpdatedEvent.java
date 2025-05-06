package com.example.user.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserUpdatedEvent {
    private UUID id;
    private Long userId;
    private String nickname;
}
