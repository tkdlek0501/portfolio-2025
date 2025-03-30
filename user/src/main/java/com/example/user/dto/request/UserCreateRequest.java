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

    public User toEntity(PasswordEncoder passwordEncoder) {

        return User.builder()
                .name(this.name())
                .password(User.encodePassword(this.password, passwordEncoder))
                .nickname(this.nickname)
                .phone(this.phone)
                .email(this.email)
                .role(this.role)
                .grade(this.grade)
                .status(UserStatus.NORMAL)
                .build();
    }
}
