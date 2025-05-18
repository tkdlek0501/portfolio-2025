package com.example.point.security;

import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider extends JwtTokenProvidable<Authentication> {

    private static final String authType = "Bearer";

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateToken(userPrincipal);
    }

    public String generateToken(UserPrincipal userPrincipal) {
        int jwtExpirationInMs = getJwtExpirationInMs();

        long nowMills = System.currentTimeMillis();
        Date now = new Date(nowMills);
        Date expiryDate = new Date(nowMills + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setClaims(makeClaimContents(userPrincipal, expiryDate))
                .signWith(getSigningKey()) // 비밀키를 사용하여 서명
                .compact();
    }

    private Map<String, Object> makeClaimContents(UserPrincipal userPrincipal, Date expireDate) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", userPrincipal.getId());
        claims.put("name", userPrincipal.getName());
        claims.put("nickname", userPrincipal.getNickname());
        claims.put("phone", userPrincipal.getPhone());
        claims.put("email", userPrincipal.getEmail());
        claims.put("role", userPrincipal.getRole().name());
        claims.put("grade", userPrincipal.getGrade().name());
        claims.put("status", userPrincipal.getStatus().name());
        claims.put("type", authType);
        claims.put("expireAt", expireDate);

        return claims;
    }
}
