package com.example.board.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

  private final String code;
  private final String message;

  public GlobalException(String code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }
}
