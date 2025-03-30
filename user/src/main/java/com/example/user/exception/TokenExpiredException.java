package com.example.user.exception;

public class TokenExpiredException extends GlobalException {
    public TokenExpiredException() {
        super(ErrorMessage.TOKEN_EXPIRED.getCode(), ErrorMessage.TOKEN_EXPIRED.getMessage());
    }
}
