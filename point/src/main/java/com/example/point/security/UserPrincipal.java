package com.example.point.security;

import com.example.point.domain.enums.UserGrade;
import com.example.point.domain.enums.UserRole;
import com.example.point.domain.enums.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
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

    public static UserDetails of(Long userId, String name, String nickname, UserRole role, UserGrade grade) {

        return UserPrincipal.builder()
                .id(userId)
                .name(name)
                .nickname(nickname)
                .role(role)
                .grade(grade)
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
