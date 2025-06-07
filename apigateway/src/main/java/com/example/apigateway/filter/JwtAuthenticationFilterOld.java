//package com.example.apigateway.filter;
//
//import com.example.apigateway.jwt.JwtTokenProvidable;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.ReactiveSecurityContextHolder;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextImpl;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class JwtAuthenticationFilterOld implements WebFilter { // WebFilter 대신 AbstractGatewayFilterFactory 을 상속받는 게 더 적절하다
//
//    private final JwtTokenProvidable<?> loginJwtTokenProvider;
//    private final ReactiveRedisTemplate<String, String> redisTemplate;
//
//    private static final List<String> NON_FILTER_PATTERN = List.of(
//            "/api/user-server/auth/login", "/api/user-server/users/sign-up", "/swagger", "/v3/api-docs", "/h2-console"
//    );
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        ServerHttpRequest request = exchange.getRequest();
//        String requestURI = request.getURI().getPath();
//
//        // 필터 제외 경로
//        if (NON_FILTER_PATTERN.stream().anyMatch(requestURI::contains)) {
//            return chain.filter(exchange);
//        }
//
//        // Authorization 헤더 로그 찍기
//        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//        log.info("[api-gateway-server] Authorization Header: {}", authHeader);
//
//        String jwt = getJwtFromRequest(request);
//        ServerHttpResponse response = exchange.getResponse();
//
//        if (!StringUtils.hasText(jwt) || !loginJwtTokenProvider.validateToken(jwt)) {
//            log.info("[api-gateway-server] 유효하지 않은 토큰 jwt : {}", jwt);
//            return unauthorizedResponse(response, "[api-gateway-server] 현재 토큰이 유효하지 않습니다. jwt : " + jwt);
//        }
//
//        String userId = loginJwtTokenProvider.getUserIdFromJWT(jwt);
//        String blacklistKey = "BL_" + userId;
//
//        return redisTemplate.hasKey(blacklistKey)
//                .flatMap(isBlacklisted -> {
//                    if (Boolean.TRUE.equals(isBlacklisted)) {
//                        log.info("[api-gateway-server] 로그아웃 처리된 토큰 jwt : {}", jwt);
//                        return unauthorizedResponse(response, "로그아웃된 토큰입니다.");
//                    }
//
//                    // 인증 객체 생성
//                    UserDetails userDetails = loginJwtTokenProvider.getUserDetails(jwt);
//                    Authentication authentication = new UsernamePasswordAuthenticationToken(
//                            userDetails, null, userDetails.getAuthorities());
//                    SecurityContext context = new SecurityContextImpl(authentication);
//
//                    log.info("[api-gateway-server] 최종 Request URI: {}", request.getURI());
//                    log.info("[api-gateway-server] 최종 Authorization Header: {}", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
//
//                    return chain.filter(exchange)
//                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
//                });
//    }
//
//    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
//        response.setStatusCode(HttpStatus.UNAUTHORIZED);
//        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
//        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
//    }
//
//    private String getJwtFromRequest(ServerHttpRequest request) {
//        HttpHeaders headers = request.getHeaders();
//        String bearerToken = headers.getFirst("Authorization");
//
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}
