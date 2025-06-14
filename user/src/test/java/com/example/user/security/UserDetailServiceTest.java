package com.example.user.security;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserGrade;
import com.example.user.domain.enums.UserRole;
import com.example.user.domain.enums.UserStatus;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.filter.UserContext;
import com.example.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private JwtTokenProvider jwtTokenProvider;

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

        // mock된 expiration 시간: 현재 시간 + 1시간
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofHours(1));
        String expirationStr = String.valueOf(expiration.toEpochMilli()); // ISO 8601 format

        // mock static method
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getExpiration).thenReturn(expirationStr);

            // JSON 문자열 mock
            String expectedJson = "{\"reason\":\"로그아웃\",\"timestamp\":\"2025-06-07T12:00:00\"}";
            when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);

            // when
            userDetailService.addBlackList(userId, reason);

            // then
            ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
            verify(valueOperations).set(eq("BL_" + userId), eq(expectedJson), ttlCaptor.capture(), eq(TimeUnit.SECONDS));

            // TTL이 3500~3600초 사이인지 확인 (1시간 범위 오차)
            Long capturedTtl = ttlCaptor.getValue();
            assertTrue(capturedTtl >= 3500 && capturedTtl <= 3600, "TTL이 예상 범위 안에 있어야 합니다.");
        }
    }

    @DisplayName("addBlackList_Json변환에러_로그출력")
    @Test
    void addBlackList_Json변환에러_로그출력() throws JsonProcessingException {
        // given
        Long userId = 1L;
        String reason = "로그아웃";

        // mock된 expiration 시간: 현재 시간 + 1시간
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofHours(1));
        String expirationStr = String.valueOf(expiration.toEpochMilli()); // ISO 8601 format

        // JsonProcessingException은 추상 클래스이므로 mock 생성
        JsonProcessingException exception = mock(JsonProcessingException.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(exception);

        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getExpiration).thenReturn(expirationStr);

            // when
            userDetailService.addBlackList(userId, reason);

            // then: Redis에 저장 시도 없어야 함
            verify(valueOperations, never()).set(any(), any(), anyLong(), any());
        }
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
