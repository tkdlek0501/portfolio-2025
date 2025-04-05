package com.example.user.dto.request;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;

public record UserCreateRequest(
        @NotBlank
        String name,
        @NotBlank
        String password,
        @NotBlank
        String nickname,
        @NotBlank
        String phone,
        @NotBlank
        String email,
        @NotNull
        UserRole role,
        @NotNull
        UserGrade grade
) {
}
