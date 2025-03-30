package com.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        String name,
        @NotBlank
        String password
) {
}
