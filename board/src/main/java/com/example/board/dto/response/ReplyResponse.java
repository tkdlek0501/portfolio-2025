package com.example.board.dto.response;

import java.time.LocalDateTime;

public record ReplyResponse(
        long id,
        String nickname,
        String content,
        LocalDateTime createdDateTime,
        LocalDateTime updatedDateTime
) {
    public static ReplyResponse of(long id, String nickname, String content, LocalDateTime createdDateTime, LocalDateTime updatedDateTime) {
        return new ReplyResponse(id, nickname, content, createdDateTime, updatedDateTime);
    }
}
