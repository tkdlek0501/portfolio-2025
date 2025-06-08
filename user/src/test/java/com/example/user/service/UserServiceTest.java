package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import com.example.user.dto.event.UserUpdatedEvent;
import com.example.user.dto.request.UserCreateRequest;
import com.example.user.dto.request.UserUpdateRequest;
import com.example.user.dto.response.UserResponse;
import com.example.user.exception.AlreadyExistsUserException;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.filter.UserContext;
import com.example.user.repository.UserRepository;
import com.example.user.security.UserDetailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private ApplicationEventPublisher eventPublisher;

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

    @DisplayName("회원정보 수정 성공 테스트")
    @Test
    void update_success_withNicknameChange() {
        // given
        String jwt = "some.jwt.token";
        long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("newName", "newPassword", "newNick", "01011112222", "new@example.com");

        User user = mock(User.class);

        // JwtUtil.getId()가 실제 호출되는 걸 막으려면 JwtUtil을 mock 해야 함
        try (MockedStatic<UserContext> jwtMock = Mockito.mockStatic(UserContext.class)) {
            jwtMock.when(UserContext::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));
            when(userRepository.findByNameAndIdNot(request.name(), userId)).thenReturn(Optional.empty());
            when(user.isEqualsNickname(request.nickname())).thenReturn(false);

            // user.modify(...) 메서드는 void 이므로 그냥 호출만 확인
            doNothing().when(user).modify(passwordEncoder,
                    request.name(),
                    request.password(),
                    request.nickname(),
                    request.phone(),
                    request.email());

            doNothing().when(userDetailService).addBlackList(userId, "유저 정보 수정");

            // eventPublisher.publishEvent 호출 감시
            doNothing().when(eventPublisher).publishEvent(any(UserUpdatedEvent.class));

            // when
            userService.update(request);

            // then
            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(userRepository).findByNameAndIdNot(request.name(), userId);
            verify(user).isEqualsNickname(request.nickname());
            verify(user).modify(passwordEncoder,
                    request.name(),
                    request.password(),
                    request.nickname(),
                    request.phone(),
                    request.email());
            verify(userDetailService).addBlackList(userId, "유저 정보 수정");
            verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
        }
    }

    @DisplayName("회원정보 수정 시 예외 발생")
    @Test
    void update_fail_whenNameDuplicate() {
        // given
        String jwt = "some.jwt.token";
        long userId = 1L;
        UserUpdateRequest request = new UserUpdateRequest("existingName", "pass", "nick", "phone", "email");

        User user = mock(User.class);

        try (MockedStatic<UserContext> jwtMock = Mockito.mockStatic(UserContext.class)) {
            jwtMock.when(UserContext::getId).thenReturn(userId);

            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));
            when(userRepository.findByNameAndIdNot(request.name(), userId)).thenReturn(Optional.of(mock(User.class)));

            when(user.getName()).thenReturn("oldName");

            // user.getName()와 request.name()이 다르도록 설정
            when(user.getName()).thenReturn("oldName");

            // when & then
            Assertions.assertThrows(AlreadyExistsUserException.class, () -> userService.update(request));

            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(userRepository).findByNameAndIdNot(request.name(), userId);
            verify(userDetailService, never()).addBlackList(anyLong(), anyString());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @DisplayName("회원탈퇴 성공 테스트")
    @Test
    void withdrawal_success() {
        // given
        String jwt = "some.jwt.token";
        long userId = 1L;
        User user = mock(User.class);

        try (MockedStatic<UserContext> jwtMock = Mockito.mockStatic(UserContext.class)) {
            // when
            jwtMock.when(UserContext::getId).thenReturn(userId);
            when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));

            doNothing().when(user).withdrawal();
            doNothing().when(userDetailService).addBlackList(userId, "유저 탈퇴");

            // then
            userService.withdrawal();

            verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            verify(user).withdrawal();
            verify(userDetailService).addBlackList(userId, "유저 탈퇴");
        }
    }

    @DisplayName("회원정보 조회 성공 테스트")
    @Test
    void getMe_success() {
        long userId = 1L;
        User user = mock(User.class);
        UserResponse userResponse = mock(UserResponse.class);

        try (MockedStatic<UserResponse> userResponseMock = Mockito.mockStatic(UserResponse.class)) {
            try (MockedStatic<UserContext> jwtMock = Mockito.mockStatic(UserContext.class)) {
                jwtMock.when(UserContext::getId).thenReturn(userId);

                when(userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)).thenReturn(Optional.of(user));

                userResponseMock.when(() -> UserResponse.from(user)).thenReturn(userResponse);

                UserResponse result = userService.getMe();

                assertThat(result).isEqualTo(userResponse);

                verify(userRepository).findByIdAndStatus(userId, UserStatus.NORMAL);
            }
        }
    }
}
