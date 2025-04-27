package com.example.user.security;

import com.example.user.domain.entity.User;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

    // 블랙리스트 추가
    public void addBlackList(Long id, String reason) {
        // 유저 정보를 JSON 형식으로 생성
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("reason", reason);
        userInfo.put("timestamp", LocalDateTime.now().toString());

        try {
            String userJson = objectMapper.writeValueAsString(userInfo);
            // TODO: TTL jwt 만료시간과 맞추기
            redisTemplate.opsForValue().set("BL_" + id, userJson, Duration.ofMinutes(30));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    // 블랙리스트 삭제
    public void removeBlackList(Long id) {
        redisTemplate.delete("BL_" + id);
    }
}
