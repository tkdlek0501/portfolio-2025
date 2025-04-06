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
import com.example.user.util.jwt.JwtUtil;
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

    @DisplayName("이름으로 사용자 캐시 갱신 - 성공")
    @Test
    void updateUserInCache_success() {
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
        userService.updateUserInCache(username);

        // then
        verify(userRepository).findByName(username);
        verify(userDetailService).updateUserInCache(mockUser);
    }

    @DisplayName("캐시 업데이트 - 유저가 없으면 예외 발생")
    @Test
    void updateUserInCache_userNotFound() {
        // given
        String username = "nonExistingUser";
        when(userRepository.findByName(username)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserInCache(username));
        verify(userDetailService, never()).updateUserInCache(any());
    }

    @DisplayName("회원 정보 수정 - 성공")
    @Test
    void update_success() {
        // given
        Long userId = 1L;
        String originalName = "originalUser";
        String newName = "newUser";

        UserUpdateRequest request = new UserUpdateRequest(
                newName, // 이름이 바뀜
                "newPassword",
                "newNick",
                "01012345678",
                "new@email.com"
        );

        // static mock (JwtUtil.getId())
        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));
            when(user.getName()).thenReturn(originalName); // 기존 이름
            when(userRepository.findByNameAndIdNot(newName, userId)).thenReturn(Optional.empty());

            // when
            userService.update(request);

            // then
            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(userRepository).findByNameAndIdNot(newName, userId);
            verify(user).modify(passwordEncoder, newName, "newPassword", "newNick", "01012345678", "new@email.com");
            verify(userDetailService).deleteUserInCache(originalName);
        }
    }

    @DisplayName("회원 정보 수정 - 유저 없음 예외")
    @Test
    void update_userNotFound() {
        // given
        Long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest(
                "anyName", "pw", "nick", "010", "email"
        );

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.empty());

            // when & then
            assertThrows(ResourceNotFoundException.class, () -> userService.update(request));
            verify(userRepository, never()).findByNameAndIdNot(anyString(), anyLong());
            verify(userDetailService, never()).deleteUserInCache(anyString());
        }
    }

    @DisplayName("회원 정보 수정 - 이름 중복 예외")
    @Test
    void update_usernameAlreadyExists() {
        // given
        Long userId = 1L;
        String originalName = "original";
        String duplicatedName = "duplicated";

        UserUpdateRequest request = new UserUpdateRequest(
                duplicatedName, "pw", "nick", "010", "email"
        );

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(JwtUtil::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));
            when(user.getName()).thenReturn(originalName);
            when(userRepository.findByNameAndIdNot(duplicatedName, userId)).thenReturn(Optional.of(mock(User.class))); // duplicatedName 으로 찾았을 때 User가 조회된다면

            // when & then
            assertThrows(AlreadyExistsUserException.class, () -> userService.update(request));
            verify(userRepository).findByNameAndIdNot(duplicatedName, userId);
            verify(userDetailService, never()).deleteUserInCache(any());
        }
    }

    @DisplayName("회원 탈퇴 - 성공")
    @Test
    void withdrawal_success() {
        // given
        Long userId = 1L;
        String userName = "testUser";

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(JwtUtil::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL))
                    .thenReturn(Optional.of(user));
            when(user.getName()).thenReturn(userName);

            // when
            userService.withdrawal();

            // then
            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(user).withdrawal();
            verify(userDetailService).deleteUserInCache(userName);
        }
    }

    @DisplayName("회원 탈퇴 - 유저 없음 예외")
    @Test
    void withdrawal_userNotFound() {
        // given
        Long userId = 1L;

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(JwtUtil::getId).thenReturn(userId);
            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL))
                    .thenReturn(Optional.empty());

            // when & then
            assertThrows(ResourceNotFoundException.class, () -> userService.withdrawal());

            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(user, never()).withdrawal();
            verify(userDetailService, never()).deleteUserInCache(any());
        }
    }

    @Test
    void getMe_success() {
        // given
        Long mockUserId = 1L;
        User mockUser = mock(User.class);
        given(userRepository.findByIdAndStatus(mockUserId, UserStatus.NORMAL)).willReturn(Optional.of(mockUser));

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(JwtUtil::getId).thenReturn(mockUserId);

            // when
            UserResponse result = userService.getMe();

            // then
            assertNotNull(result);
            verify(userRepository).findByIdAndStatus(mockUserId, UserStatus.NORMAL);
        }
    }

    @Test
    void getMe_userNotFound_throwsException() {
        // given
        Long mockUserId = 1L;
        given(userRepository.findByIdAndStatus(mockUserId, UserStatus.NORMAL)).willReturn(Optional.empty());

        try (MockedStatic<JwtUtil> jwtUtil = mockStatic(JwtUtil.class)) {
            jwtUtil.when(JwtUtil::getId).thenReturn(mockUserId);

            // when & then
            assertThrows(ResourceNotFoundException.class, () -> userService.getMe());
            verify(userRepository).findByIdAndStatus(mockUserId, UserStatus.NORMAL);
        }
    }
}
