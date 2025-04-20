package com.example.board.domain.enums;

import lombok.Getter;

@Getter
public enum UserGrade {

    BRONZE("브론즈"),
    SILVER("실버"),
    GOLD("골드"),
    ;

    private final String description;

    UserGrade(String description) {
        this.description = description;
    }
}
