package com.example.board.domain.enums;

import lombok.Getter;

@Getter
public enum UserRole {

    NORMAL("일반"),
    ADMIN("관리자"),
    ;

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
