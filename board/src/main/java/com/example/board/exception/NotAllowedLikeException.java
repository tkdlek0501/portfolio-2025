package com.example.board.exception;

public class NotAllowedLikeException extends GlobalException {
    public NotAllowedLikeException() {
        super(ErrorMessage.NOT_ALLOWED_LIKE.getCode(), ErrorMessage.NOT_ALLOWED_LIKE.getMessage());
    }
}
