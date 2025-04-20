package com.example.board.dto.response;

public record PostResponse(
        long id,
        String nickname,
        String title,
        String content,
        int viewCount,
        int likeCount
) {
    public static PostResponse of(long id, String nickname, String title, String content, int viewCount, int likeCount) {
        return new PostResponse(id, nickname, title, content, viewCount, likeCount);
    }
}
