package com.example.board.exception;

public class NotAllowedPostCategoryException extends GlobalException {
    public NotAllowedPostCategoryException() {
        super(ErrorMessage.NOT_ALLOWED_POST_CATEGORY.getCode(), ErrorMessage.NOT_ALLOWED_POST_CATEGORY.getMessage());
    }
}
