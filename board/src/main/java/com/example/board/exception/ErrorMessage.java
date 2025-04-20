package com.example.board.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessage implements BaseCodeMessage {

    // system 적 에러
    RESOURCE_NOT_FOUND("SYS001", "해당 자원을 찾지 못했습니다."),

    // 인증, 인가 에러
    TOKEN_EXPIRED("JWT001", "해당 JWT는 만료되었습니다."),

    // 게시글 에러
    NOT_ALLOWED_POST("PST001", "게시글을 수정할 권한이 없습니다.")
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
