package com.example.point.domain.enums;

import lombok.Getter;

@Getter
public enum UserStatus {

    NORMAL("일반"),
    WITHDRAWAL("탈퇴"),
    ;


    private String description;

    UserStatus(String description) {
        this.description = description;
    }
}
