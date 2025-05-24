package com.example.board.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostViewIncreasedEvent {
    private Long postId;
    private Long userId;
}
