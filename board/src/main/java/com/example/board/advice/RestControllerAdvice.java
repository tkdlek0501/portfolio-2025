package com.example.board.advice;

import com.example.board.dto.response.GlobalResponse;
import com.example.board.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice(annotations = RestController.class)
public class RestControllerAdvice {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse> handleException(HttpServletRequest request, Exception e) {
        return ResponseEntity.ok(GlobalResponse.of(e));
    }

    // 403 예외 처리
    @ResponseBody
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(HttpServletRequest request, AccessDeniedException e) {
        // AccessDeniedException을 다시 던지면 필터에서 잡힌다.
        throw new AccessDeniedException("접근이 거부되었습니다.");
    }

    // 401 예외 처리
    @ResponseBody
    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthenticationException(HttpServletRequest request, AuthenticationException e) {
        // AuthenticationException을 다시 던지면 필터에서 잡힌다.
        throw new AuthenticationException("인증 실패") {};
    }

    @ResponseBody
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<GlobalResponse> handleGlobalException(GlobalException e) {
        return ResponseEntity.ok(GlobalResponse.of(e));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(GlobalResponse.of(e));
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);

        return ResponseEntity.badRequest().body(GlobalResponse.ofError(fieldError.getField(), fieldError.getDefaultMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<GlobalResponse> handleissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(GlobalResponse.ofError(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getReasonPhrase()));
    }
}
