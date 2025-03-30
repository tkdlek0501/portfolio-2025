package com.example.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage implements BaseCodeMessage {

    // system 적 에러
    RESOURCE_NOT_FOUND("SYS001", "해당 자원을 찾지 못했습니다."),

    // 인증, 인가 에러
    TOKEN_EXPIRED("JWT001", "해당 JWT는 만료되었습니다."),

    // 회원 관련 에러
    ALREADY_EXISTS_USER("USR001", "이미 같은 이름의 계정이 있습니다. 다른 이름을 사용해주세요.")
    ;

    private final String code;
    private final String message;

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
