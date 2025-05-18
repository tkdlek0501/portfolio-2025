package com.example.board.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReplyCreatedEvent {
    private UUID id;
    private Long replyId;
    private Long userId;
    private int point;
}
