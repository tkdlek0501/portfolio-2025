package com.example.user.security;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserStatus;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
// 인증(Authentication) 을 위해 유저 정보를 제공
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("user"));
        return UserPrincipal.of(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserFromCacheOrDB(String name) {
        // Redis 에서 사용자 정보를 조회
        String userJson = redisTemplate.opsForValue().get("user:" + name);

        if (userJson != null) {
            try {
                User user = objectMapper.readValue(userJson, User.class);
                log.info("redis 에서 정상적으로 user 를 가져옴");
                return UserPrincipal.of(user); // early return
            } catch (JsonProcessingException e) {
                log.error("Redis 의 user 데이터 JSON 역직렬화 실패");
            }
        }

        // user 가 자신의 정보를 수정하면 redis 에 있는 정보를 삭제
        // 그래야 필터를 타면서 redis 에 데이터가 없는 상황이 발생하고 db 의 정보로 다시 redis 에 저장할 수 있다.
        // Redis 에 정보가 없으면 DB 에서 조회
        User user = userRepository.findByNameAndStatus(name, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        // DB 에서 가져온 정보를 Redis 에 저장
        updateUserInCache(user);

        return UserPrincipal.of(user);
    }

    // 새 로그인 시 JWT 발급 후 Redis 갱신
    public void updateUserInCache(User user) {
        try {
            String userJson = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set("user:" + user.getName(), userJson, Duration.ofMinutes(30));
        } catch (JsonProcessingException e) {
            log.error("error : ", e);
        }
    }

    // Redis 에 있는 유저 정보 삭제
    public void deleteUserInCache(String name) {
        redisTemplate.delete("user:" + name);
    }
}
