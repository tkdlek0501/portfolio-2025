package com.example.point.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PostCreatedEvent {
    private UUID id;
    private Long postId;
    private Long userId;
    private int point;
}
