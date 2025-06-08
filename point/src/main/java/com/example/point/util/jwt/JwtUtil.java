//package com.example.point.util.jwt;
//
//import com.example.point.security.UserPrincipal;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//public class JwtUtil {
//
//    public static long getId() {
//        Object authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication instanceof UsernamePasswordAuthenticationToken) {
//            UserPrincipal principal = (UserPrincipal) ((UsernamePasswordAuthenticationToken) authentication).getPrincipal();
//
//            return principal.getId();
//        } else {
//            return -1;
//        }
//    }
//
//    public static String getNickname() {
//        Object authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication instanceof UsernamePasswordAuthenticationToken) {
//            UserPrincipal principal = (UserPrincipal) ((UsernamePasswordAuthenticationToken) authentication).getPrincipal();
//
//            return principal.getNickname();
//        } else {
//            return "";
//        }
//    }
//}
