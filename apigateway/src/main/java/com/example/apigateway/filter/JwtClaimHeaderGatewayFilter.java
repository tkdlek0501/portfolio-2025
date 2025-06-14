package com.example.apigateway.filter;

import com.example.apigateway.jwt.JwtTokenProvidable;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// 라우팅 필터
@Slf4j
@Component
public class JwtClaimHeaderGatewayFilter extends AbstractGatewayFilterFactory<JwtClaimHeaderGatewayFilter.Config> {

    private final JwtTokenProvidable<?> loginJwtTokenProvider;

    public static class Config {
        // 설정이 필요한 경우 여기에 추가
    }

    public JwtClaimHeaderGatewayFilter(JwtTokenProvidable<?> loginJwtTokenProvider) {
        super(Config.class); // 이 부분이 꼭 필요
        this.loginJwtTokenProvider = loginJwtTokenProvider;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String jwt = extractToken(exchange.getRequest());

            if (StringUtils.hasText(jwt) && loginJwtTokenProvider.validateToken(jwt)) {
                Claims claims = loginJwtTokenProvider.getClaims(jwt);

                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-USER-ID", String.valueOf(claims.get("id")))
                        .header("X-USER-NAME", String.valueOf(claims.get("name")))
                        .header("X-USER-NICKNAME", String.valueOf(claims.get("nickname")))
                        .header("X-USER-ROLE", String.valueOf(claims.get("role")))
                        .header("X-USER-GRADE", String.valueOf(claims.get("grade")))
                        .header("X-USER-EXPIRATION", String.valueOf(claims.get("expireAt")))
                        .build();

                exchange = exchange.mutate().request(mutatedRequest).build();
            }

            return chain.filter(exchange);
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
