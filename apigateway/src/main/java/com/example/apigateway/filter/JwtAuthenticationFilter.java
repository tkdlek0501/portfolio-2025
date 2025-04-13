package com.example.apigateway.filter;

import com.example.apigateway.jwt.JwtTokenProvidable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtTokenProvidable<?> loginJwtTokenProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final List<String> NON_FILTER_PATTERN = List.of(
            "/api/users/auth/login", "/api/users/sign-up", "/swagger", "/v3/api-docs", "/h2-console"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestURI = request.getURI().getPath();

        // 필터 제외 경로
        if (NON_FILTER_PATTERN.stream().anyMatch(requestURI::contains)) {
            return chain.filter(exchange);
        }

        String jwt = getJwtFromRequest(request);
        ServerHttpResponse response = exchange.getResponse();

        if (!StringUtils.hasText(jwt) || !loginJwtTokenProvider.validateToken(jwt)) {
            return unauthorizedResponse(response, "현재 토큰이 유효하지 않습니다.");
        }

        String username = loginJwtTokenProvider.getUsernameFromJWT(jwt);
        String blacklistKey = "BL_" + username;

        return redisTemplate.hasKey(blacklistKey)
                .flatMap(isBlacklisted -> {
                    if (Boolean.TRUE.equals(isBlacklisted)) {
                        return unauthorizedResponse(response, "로그아웃된 토큰입니다.");
                    }

                    String userId = loginJwtTokenProvider.getUserIdFromJWT(jwt);
                    String userRole = loginJwtTokenProvider.getUserRoleFromJWT(jwt);
                    String userGrade = loginJwtTokenProvider.getUserGradeFromJWT(jwt);

                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-ID", userId)
                            .header("X-User-Role", userRole)
                            .header("X-User-Grade", userGrade)
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });

//        if (StringUtils.hasText(jwt) && loginJwtTokenProvider.validateToken(jwt)) {
//            // 블랙리스트 체크
//            Boolean isBlacklisted = redisTemplate.hasKey("BL_" + loginJwtTokenProvider.getUsernameFromJWT(jwt));
//            if (Boolean.TRUE.equals(isBlacklisted)) {
//                response.setStatusCode(HttpStatus.UNAUTHORIZED);
//                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
//                return response.writeWith(Mono.just(response.bufferFactory().wrap(
//                        "{\"code\":401,\"message\":\"로그아웃된 토큰입니다.\",\"data\":null}".getBytes()
//                )));
//            }
//
//            // 헤더에 사용자 정보 추가
//            String userId = loginJwtTokenProvider.getUserIdFromJWT(jwt);
//            String userRole = loginJwtTokenProvider.getUserRoleFromJWT(jwt);
//            String userGrade = loginJwtTokenProvider.getUserGradeFromJWT(jwt);
//
//            ServerHttpRequest mutatedRequest = request.mutate()
//                    .header("X-User-ID", userId)
//                    .header("X-User-Role", userRole)
//                    .header("X-User-Grade", userGrade)
//                    .build();
//
//            return chain.filter(exchange.mutate().request(mutatedRequest).build());
//        } else {
//            response.setStatusCode(HttpStatus.UNAUTHORIZED);
//            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
//            return response.writeWith(Mono.just(response.bufferFactory().wrap(
//                    "{\"code\":401,\"message\":\"현재 토큰이 유효하지 않습니다.\",\"data\":null}".getBytes()
//            )));
//        }
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private String getJwtFromRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String bearerToken = headers.getFirst("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
