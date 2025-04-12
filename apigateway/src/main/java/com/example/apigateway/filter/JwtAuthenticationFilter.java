package com.example.apigateway.filter;

import com.example.apigateway.jwt.JwtTokenProvidable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvidable<Authentication> loginJwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    private final List<String> NON_FILTER_PATTERN = Arrays.asList(
            "/api/users/auth/login", "/api/users/sign-up"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/swagger") || requestURI.contains("/v3/api-docs") || requestURI.contains("/h2-console") ||
                NON_FILTER_PATTERN.stream().anyMatch(requestURI::contains)) {
            filterChain.doFilter(request, response);
            return; // 로그인 url 을 포함한 불필요한 url 은 여기 필터에서 제외
        }

        String jwt = getJwtFromRequest(request);

        // 토큰이 있고 유효한 경우에만 인증 처리
        if (StringUtils.hasText(jwt) && loginJwtTokenProvider.validateToken(jwt)) {
            // 블랙리스트 확인
            Boolean isBlacklisted = redisTemplate.hasKey("BL_" + loginJwtTokenProvider.getUsernameFromJWT(jwt));
            if (Boolean.TRUE.equals(isBlacklisted)) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(
                        String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.")
                );
                return;
            }

            String userId = loginJwtTokenProvider.getUserIdFromJWT(jwt);
            String userRole = loginJwtTokenProvider.getUserRoleFromJWT(jwt);
            String userGrade = loginJwtTokenProvider.getUserGradeFromJWT(jwt);

            // 헤더에 사용자 정보 추가
            request.setAttribute("X-User-ID", userId);
            request.setAttribute("X-User-Role", userRole);
            request.setAttribute("X-User-Grade", userGrade);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(
                    String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", HttpServletResponse.SC_UNAUTHORIZED, "현재 토큰이 유효하지 않습니다.")
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearToken) && bearToken.startsWith("Bearer ")) {
            return bearToken.substring(7);
        }

        return null;
    }
}
