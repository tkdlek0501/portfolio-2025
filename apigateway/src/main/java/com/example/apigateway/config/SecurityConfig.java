package com.example.apigateway.config;

import com.example.apigateway.filter.JwtAuthenticationFilter;

import com.example.apigateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private static final String[] IGNORE_LIST = {
            // Common=

            // User
            "/api/users/v3/api-docs", "/api/users/v3/api-docs/**", "/api/users/swagger-ui/**", "/api/users/swagger-resources/**",
            "/api/users/h2-console/**", "/api/users/webjars/**", "/api/users/favicon.**",
            "/api/users/auth/login", "/api/users/sign-up",

            // Board
            "/api/boards/v3/api-docs", "/api/boards/v3/api-docs/**", "/api/boards/swagger-ui/**", "/api/boards/swagger-resources/**",
            "/api/boards/h2-console/**", "/api/boards/webjars/**", "/api/boards/favicon.**",

            // Point
    };

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate);
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
//        return configuration.getAuthenticationManager();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF 비활성화
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(IGNORE_LIST).permitAll() // 인증이 필요 없는 경로들
                        .anyExchange().authenticated() // 그 외 모든 요청은 인증 필요
                )
                .addFilterBefore(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION) // JWT 인증 필터 추가
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));  // Allow all origins
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow methods
        config.setAllowedHeaders(List.of("*"));  // Allow all headers
        config.setAllowCredentials(true); // Allow credentials
        config.setMaxAge(3600L); // Cache preflight response for 1 hour

        return request -> config;
    }
}
