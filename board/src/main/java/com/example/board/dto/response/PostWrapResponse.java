package com.example.board.dto.response;

import org.springframework.data.domain.Page;

public record PostWrapResponse(
        Page<PostResponse> posts
) {
    public static PostWrapResponse of(Page<PostResponse> posts) {
        return new PostWrapResponse(posts);
    }
}
