package com.example.board.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class UserUpdatedEvent {
    private Long userId;
    private String nickname;
}
