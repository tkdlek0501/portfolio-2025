package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.dto.request.UserCreateRequest;
import com.example.user.exception.AlreadyExistsUserException;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.UserRepository;
import com.example.user.security.UserDetailService;
import com.example.user.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDetailService userDetailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserCreateRequest request) {
        if (userRepository.findByName(request.name()).isPresent()) {
            throw new AlreadyExistsUserException();
        }

        User user = request.toEntity(passwordEncoder);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserByName(String name) {

        return userRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("user"));
    }

    @Transactional(readOnly = true)
    public void updateUserInCache(String name) {
        User user = getUserByName(name);
        userDetailService.updateUserInCache(user);
    }
}
