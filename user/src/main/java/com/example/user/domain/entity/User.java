package com.example.user.domain.entity;

import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    @Column(name = "phone", length = 11, nullable = false)
    private String phone;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "role", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "grade", length = 100, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserGrade grade;

    @Column(name = "createdDate", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updatedDate", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    // 패스워드 암호화
    public static String encodePassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.encode(password);
    }

    public static User create(PasswordEncoder passwordEncoder,
                              String name,
                              String password,
                              String nickname,
                              String phone,
                              String email,
                              UserRole role,
                              UserGrade grade
    ) {
        return User.builder()
                .name(name)
                .password(User.encodePassword(password, passwordEncoder))
                .nickname(nickname)
                .phone(phone)
                .email(email)
                .role(role)
                .grade(grade)
                .status(UserStatus.NORMAL)
                .build();
    }

    public void modify(
            PasswordEncoder passwordEncoder,
            String name,
            String password,
            String nickname,
            String phone,
            String email
    ) {
        this.name = name;
        this.password = encodePassword(password, passwordEncoder);
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.updatedDate = LocalDateTime.now();
    }

    public void withdrawal() {
        this.status = UserStatus.WITHDRAWAL;
        this.updatedDate = LocalDateTime.now();
    }
}
