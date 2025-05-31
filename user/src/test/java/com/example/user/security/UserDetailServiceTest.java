package com.example.user.security;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserDetailService userDetailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations); // redisTemplate.opsForValue() 을 valueOperations 로 대체
        lenient().when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        // lenient() : 모든 테스트에서 쓰이지 않아도 됨 - stub 은 테스트에서 안 쓰여도 돼
        testUser = User.create(
                passwordEncoder,
                "testuser",
                "password123",
                "테스터",
                "01012345678",
                "test@example.com",
                UserRole.NORMAL,
                UserGrade.BRONZE
        );
    }

    @DisplayName("loadUserByUsername_정상작동")
    @Test
    void loadUserByUsername_정상작동() {
        // given
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = userDetailService.loadUserByUsername("testuser");

        // then
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
    }

    @DisplayName("loadUserByUsername_없는유저_예외발생")
    @Test
    void loadUserByUsername_없는유저_예외발생() {
        // given
        when(userRepository.findByName("nouser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userDetailService.loadUserByUsername("nouser"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("addBlackList_정상작동")
    @Test
    void addBlackList_정상작동() throws JsonProcessingException {
        // given
        Long userId = 1L;
        String reason = "로그아웃";
        String expectedJson = "{\"reason\":\"로그아웃\",\"timestamp\":\"2025-05-31T12:00:00\"}";

        // mock objectMapper
        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);
        // 아래 메서드에서 String userJson 부분
        // when
        userDetailService.addBlackList(userId, reason);

        // then
        verify(valueOperations).set(eq("BL_" + userId), eq(expectedJson), eq(Duration.ofMinutes(30)));
    }

    @DisplayName("addBlackList_Json변환에러_로그출력")
    @Test
    void addBlackList_Json변환에러_로그출력() throws JsonProcessingException {
        // given
        Long userId = 1L;
        String reason = "로그아웃";

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        // when
        userDetailService.addBlackList(userId, reason);

        // then
        verify(valueOperations, never()).set(any(), any(), any());
    }

    @DisplayName("removeBlackList_정상작동")
    @Test
    void removeBlackList_정상작동() {
        // given
        Long userId = 1L;

        // when
        userDetailService.removeBlackList(userId);

        // then
        verify(redisTemplate).delete("BL_" + userId);
    }
}
