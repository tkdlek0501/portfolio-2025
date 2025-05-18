package com.example.apigateway.config;

import com.example.apigateway.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] IGNORE_LIST = {
            // gateway
            "/swagger-ui", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
            "/webjars/**", "/swagger-resources/**", "/favicon.ico",

            // User
            "/api/user-server/v3/api-docs", "/api/user-server/v3/api-docs/**", "/api/user-server/swagger-ui/**", "/api/user-server/swagger-resources/**",
            "/api/user-server/h2-console/**", "/api/user-server/webjars/**", "/api/user-server/favicon.**",
            "/api/user-server/auth/login", "/api/user-server/users/sign-up",

            // Board
            "/api/board-server/v3/api-docs", "/api/board-server/v3/api-docs/**", "/api/board-server/swagger-ui/**", "/api/board-server/swagger-resources/**",
            "/api/board-server/h2-console/**", "/api/board-server/webjars/**", "/api/board-server/favicon.**",

            // Point
            "/api/point-server/v3/api-docs", "/api/point-server/v3/api-docs/**", "/api/point-server/swagger-ui/**", "/api/point-server/swagger-resources/**",
            "/api/point-server/h2-console/**", "/api/point-server/webjars/**", "/api/point-server/favicon.**",
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 비활성화
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(IGNORE_LIST).permitAll() // 인증이 필요 없는 경로들
                        .anyExchange().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION) // JWT 인증 필터 추가
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:8080"); // Swagger UI 주소
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
