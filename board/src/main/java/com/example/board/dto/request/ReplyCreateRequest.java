package com.example.board.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReplyCreateRequest(
        @NotBlank
        String content
) {
}
