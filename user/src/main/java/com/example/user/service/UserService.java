package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserStatus;
import com.example.user.dto.request.UserCreateRequest;
import com.example.user.dto.request.UserUpdateRequest;
import com.example.user.dto.response.UserResponse;
import com.example.user.exception.AlreadyExistsUserException;
import com.example.user.exception.ResourceNotFoundException;
import com.example.user.repository.UserRepository;
import com.example.user.security.UserDetailService;
import com.example.user.util.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(UserCreateRequest request) {
        if (userRepository.findByName(request.name()).isPresent()) {
            throw new AlreadyExistsUserException();
        }

        User user = User.create(passwordEncoder,
                request.name(),
                request.password(),
                request.nickname(),
                request.phone(),
                request.email(),
                request.role(),
                request.grade()
        );
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

    @Transactional
    public void update(UserUpdateRequest request) {
        Long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));
        String orgUsername = user.getName();

        if (!Objects.equals(user.getName(), request.name())) {
            // 본 user 는 제외
            if (userRepository.findByNameAndIdNot(request.name(), userId).isPresent()) {
                throw new AlreadyExistsUserException();
            }
        }

        user.modify(passwordEncoder,
                request.name(),
                request.password(),
                request.nickname(),
                request.phone(),
                request.email()
        );

        userDetailService.deleteUserInCache(orgUsername);
    }

    @Transactional
    public void withdrawal() {
        Long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        user.withdrawal();

        userDetailService.deleteUserInCache(user.getName());
    }

    @Transactional(readOnly = true)
    public UserResponse getMe() {
        Long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        return UserResponse.from(user);
    }
}
