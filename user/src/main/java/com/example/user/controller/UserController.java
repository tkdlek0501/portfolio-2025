package com.example.user.controller;

import com.example.user.dto.request.UserCreateRequest;
import com.example.user.dto.request.UserUpdateRequest;
import com.example.user.dto.response.GlobalResponse;
import com.example.user.dto.response.UserResponse;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 가입", description = "user 를 생성합니다.")
    @PostMapping("/sign-up")
    public ResponseEntity<GlobalResponse<Object>> signUp(
            @Valid @RequestBody UserCreateRequest request
    ) {
        userService.signUp(request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "회원 정보 수정", description = "회원을 수정합니다.")
    @PutMapping("")
    public ResponseEntity<GlobalResponse<Object>> update(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.update(userId, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "회원 탈퇴", description = "회원을 탈퇴 처리 합니다.")
    @PatchMapping("")
    public ResponseEntity<GlobalResponse<Object>> withdrawal(
            @RequestHeader("X-User-ID") String userId
    ) {
        userService.withdrawal(userId);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "회원 조회", description = "자신의 정보를 조회합니다.")
    @GetMapping("")
    public ResponseEntity<GlobalResponse<UserResponse>> getMe(
            @RequestHeader("X-User-ID") String userId
    ) {
        return ResponseEntity.ok(GlobalResponse.of(userService.getMe(userId)));
    }
}
