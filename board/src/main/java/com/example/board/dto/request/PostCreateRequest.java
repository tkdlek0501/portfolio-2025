package com.example.board.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
        @NotNull
        Long postCategoryId,
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}
