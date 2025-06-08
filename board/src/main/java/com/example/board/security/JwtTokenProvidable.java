//package com.example.board.security;
//
//import com.example.board.exception.TokenExpiredException;
//import io.jsonwebtoken.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//
//import javax.crypto.spec.SecretKeySpec;
//import java.security.Key;
//import java.util.Date;
//
//@Slf4j
//public abstract class JwtTokenProvidable<T> {
//
//    @Value("${jwt.secret}")
//    private String jwtSecret;
//
//    @Value("${jwt.expirationInMs}")
//    private int jwtExpirationInMs;
//
//    protected Key getSigningKey() {
//        return new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS512.getJcaName());
//    }
//
//    protected String getJwtSecret() {
//        return jwtSecret;
//    }
//
//    protected int getJwtExpirationInMs() {
//        return this.jwtExpirationInMs;
//    }
//
//    public abstract String generateToken(T object);
//
//    public String getUserIdFromJWT(String token) {
//        Claims claims = this.getClaims(token);
//
//        return claims.get("id").toString();
//    }
//
//    public String getUserRoleFromJWT(String token) {
//        Claims claims = this.getClaims(token);
//
//        return claims.get("role").toString();
//    }
//
//    public String getUserGradeFromJWT(String token) {
//        Claims claims = this.getClaims(token);
//
//        return claims.get("grade").toString();
//    }
//
//    public String getUsernameFromJWT(String token) {
//        Claims claims = this.getClaims(token);
//
//        return claims.get("name").toString();
//    }
//
//    public String getNicknameFromJWT(String token) {
//        Claims claims = this.getClaims(token);
//
//        return claims.get("nickname").toString();
//    }
//
//    public Claims getClaims(String authorization) {
//        Claims claims = null;
//
//        try {
//            claims = authorization != null && !authorization.isEmpty() ?
//                    Jwts.parserBuilder()
//                            .setSigningKey(jwtSecret.getBytes())
//                            .build()
//                            .parseClaimsJws(authorization.replace("Bearer ", ""))
//                            .getBody() :
//                    null;
//        } catch (Exception exp) {
//            log.error("Claims 생성 실패");
//        }
//
//        return claims;
//    }
//
//    public boolean validateToken(String authToken) {
//        try {
//            Jws<Claims> claimsJwts = Jwts.parserBuilder()
//                    .setSigningKey(jwtSecret.getBytes())
//                    .build()
//                    .parseClaimsJws(authToken);
//
//            Date expiration = claimsJwts.getBody().getExpiration();
//
//            if (null != expiration) {
//                return !expiration.before(new Date());
//            }
//
//            return true;
//        }catch (MalformedJwtException ex) {
//            log.error("Invalid JWT token");
//            throw new TokenExpiredException();
//        } catch (ExpiredJwtException ex) {
//            log.error("Expired JWT token");
//        } catch (UnsupportedJwtException ex) {
//            log.error("Unsupported JWT token");
//        } catch (IllegalArgumentException ex) {
//            log.error("JWT claims string is empty.");
//        } catch (Exception ex) {
//            log.error("error : ", ex);
//        }
//
//        return false;
//    }
//}
