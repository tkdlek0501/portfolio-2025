package com.example.user.dto.response;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;

public record UserResponse(
        String name,
        String nickname,
        String phone,
        String email,
        UserRole role,
        UserGrade grade
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getName(),
                user.getNickname(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getGrade()
        );
    }
}
