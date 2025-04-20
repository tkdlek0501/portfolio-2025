package com.example.board.exception;

public class NotAllowedPostException extends GlobalException {
    public NotAllowedPostException() {
        super(ErrorMessage.NOT_ALLOWED_POST.getCode(), ErrorMessage.NOT_ALLOWED_POST.getMessage());
    }
}
