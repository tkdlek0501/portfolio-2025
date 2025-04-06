package com.example.user.domain.entity;

import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void create_성공() {
        // given
        String rawPassword = "password123";
        String encodedPassword = "encodedPwd";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        User user = User.create(
                passwordEncoder,
                "testuser",
                rawPassword,
                "테스터",
                "01012345678",
                "test@example.com",
                UserRole.NORMAL,
                UserGrade.BRONZE
        );

        // then
        assertThat(user.getName()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getNickname()).isEqualTo("테스터");
        assertThat(user.getPhone()).isEqualTo("01012345678");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.NORMAL);
        assertThat(user.getGrade()).isEqualTo(UserGrade.BRONZE);
        assertThat(user.getStatus()).isEqualTo(UserStatus.NORMAL);
    }

    @Test
    void modify_성공() {
        // given
        User user = User.builder()
                .name("oldname")
                .password("oldpassword")
                .nickname("oldnick")
                .phone("01000000000")
                .email("old@example.com")
                .role(UserRole.NORMAL)
                .grade(UserGrade.SILVER)
                .status(UserStatus.NORMAL)
                .build();

        String rawPassword = "newpass";
        String encodedPassword = "encodedNew";
        Mockito.when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        user.modify(passwordEncoder, "newname", rawPassword, "newnick", "01099998888", "new@example.com");

        // then
        assertThat(user.getName()).isEqualTo("newname");
        assertThat(user.getPassword()).isEqualTo(encodedPassword);
        assertThat(user.getNickname()).isEqualTo("newnick");
        assertThat(user.getPhone()).isEqualTo("01099998888");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getUpdatedDate()).isNotNull(); // 시간은 현재 시간과 비교하지 않음
    }

    @Test
    void withdrawal_성공() {
        // given
        User user = User.builder()
                .name("user")
                .password("pwd")
                .nickname("nick")
                .phone("01011112222")
                .email("email@example.com")
                .role(UserRole.NORMAL)
                .grade(UserGrade.BRONZE)
                .status(UserStatus.NORMAL)
                .build();

        // when
        user.withdrawal();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWAL);
        assertThat(user.getUpdatedDate()).isNotNull();
    }
}
