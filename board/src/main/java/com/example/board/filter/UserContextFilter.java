package com.example.board.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String userIdHeader = request.getHeader("X-USER-ID");
        String nameHeader = request.getHeader("X-USER-NAME");
        String nicknameHeader = request.getHeader("X-USER-NICKNAME");
        String roleHeader = request.getHeader("X-USER-ROLE");
        String gradeHeader = request.getHeader("X-USER-GRADE");
        String expirationHeader = request.getHeader("X-USER-EXPIRATION");

        if (userIdHeader != null) {
            try {
                Long userId = Long.valueOf(userIdHeader);
                UserContext.set(userId, nameHeader, nicknameHeader, roleHeader, gradeHeader, expirationHeader); // UserContext는 ThreadLocal 기반
            } catch (NumberFormatException e) {
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear(); // 반드시 클리어
        }
    }
}
