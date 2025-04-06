package com.example.user.advice;

import com.example.user.dto.response.GlobalResponse;
import com.example.user.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
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
    public ResponseEntity<GlobalResponse> handleException(Exception e) {
        return ResponseEntity.ok(GlobalResponse.of(e));
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

        return ResponseEntity.badRequest().body(GlobalResponse.of(fieldError.getField(), fieldError.getDefaultMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ResponseEntity<GlobalResponse> handleissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(GlobalResponse.of(HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getReasonPhrase()));
    }

    // 비밀번호 틀렸을 경우
    @ResponseBody
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalResponse> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.badRequest().body(GlobalResponse.of("argument", "비밀번호가 일치하지 않습니다."));
    }

    // id가 틀렸을 경우
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<GlobalResponse> handleInternalAuthenticationServiceException(InternalAuthenticationServiceException e) {
        return ResponseEntity.badRequest().body(GlobalResponse.of("argument", "존재하지 않는 ID 입니다."));
    }
}
