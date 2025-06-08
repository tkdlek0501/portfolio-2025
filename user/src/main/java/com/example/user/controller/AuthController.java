package com.example.user.controller;

import com.example.user.dto.request.LoginRequest;
import com.example.user.dto.response.GlobalResponse;
import com.example.user.filter.UserContext;
import com.example.user.security.JwtTokenProvider;
import com.example.user.security.UserDetailService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserDetailService userDetailService;

    @Operation(summary = "로그인", description = "로그인을 하면 JWT 를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        // 로그인 정보 확인
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.name(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 발급
        String jwt = jwtTokenProvider.generateToken(authentication);

        // 블랙리스트에서 제거
        userDetailService.removeBlackList(UserContext.getId());

        return ResponseEntity.ok(GlobalResponse.of(jwt));
    }
}
