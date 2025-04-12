package com.example.apigateway.security;

import com.example.apigateway.jwt.JwtTokenProvidable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider extends JwtTokenProvidable<Authentication> {

    private static final String authType = "Bearer";

    public String generateToken(Authentication authentication) {
        return null;
    }

}
