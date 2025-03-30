package com.example.user.exception;

public class AlreadyExistsUserException extends GlobalException {
    public AlreadyExistsUserException() {
        super(ErrorMessage.ALREADY_EXISTS_USER.getCode(), ErrorMessage.ALREADY_EXISTS_USER.getMessage());
    }
}
