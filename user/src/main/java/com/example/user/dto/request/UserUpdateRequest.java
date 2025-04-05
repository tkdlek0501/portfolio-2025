package com.example.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank
        String name,
        @NotBlank
        String password,
        @NotBlank
        String nickname,
        @NotBlank
        String phone,
        @NotBlank
        String email
) {
}
