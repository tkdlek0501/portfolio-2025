package com.example.user.exception;

public class ResourceNotFoundException extends GlobalException {
    public ResourceNotFoundException(String message) {
        super(ErrorMessage.RESOURCE_NOT_FOUND.getCode(), message.concat("를 찾지 못했습니다."));
    }
}
