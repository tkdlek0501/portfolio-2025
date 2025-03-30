package com.example.user.security;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String nickname;
    private final String password;
    private final String phone;
    private final String email;
    private final UserRole role;
    private final UserGrade grade;
    private final UserStatus status;

    public static UserDetails of(User user) {

        return UserPrincipal.builder()
                .id(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .grade(user.getGrade())
                .status(user.getStatus())
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return this.name;
    }
}
