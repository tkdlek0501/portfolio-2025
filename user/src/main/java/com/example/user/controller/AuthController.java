package com.example.user.controller;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.response.GlobalResponse;
import com.example.user.security.JwtTokenProvider;
import com.example.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Operation(summary = "로그인", description = "로그인을 하면 JWT 를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        // 로그인 정보 확인
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.name(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 발급
        String jwt = jwtTokenProvider.generateToken(authentication);

        // JWT 발급 후 Redis 에 user 정보 저장
        userService.updateUserInCache(request.name());

        return ResponseEntity.ok(GlobalResponse.of(jwt));
    }
}
