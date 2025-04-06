package com.example.user.filter;

import com.example.user.security.UserDetailService;
import com.example.user.service.UserService;
import com.example.user.util.jwt.JwtTokenProvidable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvidable<Authentication> loginJwtTokenProvider;
    private final UserDetailService userDetailService;

    private final List<String> NON_FILTER_PATTERN = Arrays.asList(
            "/health", "/auth/login", "/user/sign-up"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/swagger") || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/h2-console") ||
                NON_FILTER_PATTERN.stream().anyMatch(requestURI::contains)) {
            filterChain.doFilter(request, response);
            return; // 로그인 url 을 포함한 불필요한 url 은 여기 필터에서 제외
        }

        String jwt = getJwtFromRequest(request);

        // 토큰이 있고 유효한 경우에만 인증 처리
        if (StringUtils.hasText(jwt) && loginJwtTokenProvider.validateToken(jwt)) {
            String name = loginJwtTokenProvider.getUserNameFromJWT(jwt);

            // Redis or DB 에서 사용자 정보를 확인 (최신 상태)
            UserDetails user;
            try {
                user = userDetailService.loadUserFromCacheOrDB(name);
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(
                        String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않거나 탈퇴한 유저입니다.")
                );
                return;
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            user, null, user.getAuthorities());
            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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
