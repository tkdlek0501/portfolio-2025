package com.example.user.controller;

import com.example.user.dto.request.UserCreateRequest;
import com.example.user.dto.response.GlobalResponse;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    public ResponseEntity<GlobalResponse<Object>> signUp(
            @Valid @RequestBody UserCreateRequest request
    ) {
        userService.signUp(request);
        return ResponseEntity.ok(GlobalResponse.of());
    }
}
