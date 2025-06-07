package com.example.apigateway.filter;

import com.example.apigateway.jwt.JwtTokenProvidable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {

    private final JwtTokenProvidable<?> loginJwtTokenProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final List<String> NON_FILTER_PATTERN = List.of(
            "/api/user-server/auth/login", "/api/user-server/users/sign-up", "/swagger", "/v3/api-docs", "/h2-console",
            "/api/point-server/actuator/prometheus", "/api/point-server/metrics"
    );

    public JwtFilter(JwtTokenProvidable<?> loginJwtTokenProvider,
                     ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class); // 이 부분이 꼭 필요
        this.loginJwtTokenProvider = loginJwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    public static class Config {
        // 설정이 필요한 경우 여기에 추가
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String requestURI = request.getURI().getPath();
            log.info("[api-gateway-server] requestURI: {}", requestURI);

            if (NON_FILTER_PATTERN.stream().anyMatch(requestURI::contains)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            log.info("[api-gateway-server] Authorization Header: {}", authHeader);

            String jwt = getJwtFromRequest(request);
            ServerHttpResponse response = exchange.getResponse();

            if (!StringUtils.hasText(jwt) || !loginJwtTokenProvider.validateToken(jwt)) {
                log.info("[api-gateway-server] 유효하지 않은 토큰 jwt : {}", jwt);
                return unauthorizedResponse(response, "[api-gateway-server] 현재 토큰이 유효하지 않습니다. jwt : " + jwt);
            }

            String userId = loginJwtTokenProvider.getUserIdFromJWT(jwt);
            String blacklistKey = "BL_" + userId;

            return redisTemplate.hasKey(blacklistKey)
                    .flatMap(isBlacklisted -> {
                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            log.info("[api-gateway-server] 로그아웃 처리된 토큰 jwt : {}", jwt);
                            return unauthorizedResponse(response, "로그아웃된 토큰입니다.");
                        }

                        UserDetails userDetails = loginJwtTokenProvider.getUserDetails(jwt);
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContext context = new SecurityContextImpl(authentication);

                        ServerHttpRequest mutatedRequest = request.mutate()
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                                .build();

                        ServerWebExchange mutatedExchange = exchange.mutate()
                                .request(mutatedRequest)
                                .build();

                        return chain.filter(mutatedExchange)
                                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
                    });
        };
    }

    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        String body = String.format("{\"status\":-1,\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    private String getJwtFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
