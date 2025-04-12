package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import com.example.user.dto.request.UserCreateRequest;
import com.example.user.dto.request.UserUpdateRequest;
import com.example.user.dto.response.UserResponse;
import com.example.user.exception.AlreadyExistsUserException;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.UserRepository;
import com.example.user.security.UserDetailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private User user;

    @InjectMocks
    private UserService userService;

    @DisplayName("회원가입 성공 테스트")
    @Test
    void signUp_success() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "user1", "password123", "닉네임", "01012345678", "user1@email.com",
                UserRole.NORMAL, UserGrade.BRONZE
        );
        when(userRepository.findByName("user1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded-pw");

        // when
        userService.signUp(request);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @DisplayName("이미 존재하는 이름으로 회원가입 시 예외 발생")
    @Test
    void signUp_alreadyExists() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "user1", "password123", "닉네임", "01012345678", "user1@email.com",
                UserRole.NORMAL, UserGrade.BRONZE
        );

        when(userRepository.findByName("user1"))
                .thenReturn(Optional.of(mock(User.class)));

        // when & then
        assertThrows(AlreadyExistsUserException.class, () -> userService.signUp(request));
        verify(userRepository, never()).save(any());
    }

    @DisplayName("이름으로 사용자 조회 - 성공")
    @Test
    void getUserByName_success() {
        // given
        String username = "user1";
        User mockUser = User.builder()
                .name(username)
                .password("encoded-password")
                .nickname("닉네임")
                .phone("01012345678")
                .email("user1@email.com")
                .role(UserRole.NORMAL)
                .grade(UserGrade.BRONZE)
                .build();

        when(userRepository.findByName(username)).thenReturn(Optional.of(mockUser));

        // when
        User foundUser = userService.getUserByName(username);

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo(username);
        verify(userRepository, times(1)).findByName(username);
    }

    @DisplayName("이름으로 사용자 조회 - 존재하지 않으면 예외 발생")
    @Test
    void getUserByName_notFound() {
        // given
        String username = "nonExistingUser";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByName(username));
        verify(userRepository, times(1)).findByName(username);
    }
}
