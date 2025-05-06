package com.example.board.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserUpdatedEvent {
    private UUID id;
    private Long userId;
    private String nickname;
}
