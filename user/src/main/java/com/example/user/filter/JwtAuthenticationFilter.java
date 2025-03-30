package com.example.user.filter;

import com.example.user.security.UserDetailService;
import com.example.user.util.jwt.JwtTokenProvidable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvidable<Authentication> loginJwtTokenProvider;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);

        // 토큰이 있고 유효한 경우에만 인증 처리
        if (StringUtils.hasText(jwt) && loginJwtTokenProvider.validateToken(jwt)) {
            String name = loginJwtTokenProvider.getUserNameFromJWT(jwt);

            // UserDetails 로드 로직 추가 필요
            UserDetails user = userDetailService.loadUserByUsername(name);

            if (user != null) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
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
