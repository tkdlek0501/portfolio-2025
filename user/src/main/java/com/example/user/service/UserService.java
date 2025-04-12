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

    @Transactional
    public void update(String userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndStatus(Long.valueOf(userId), UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        if (!Objects.equals(user.getName(), request.name())) {
            // 본 user 는 제외
            if (userRepository.findByNameAndIdNot(request.name(), Long.valueOf(userId)).isPresent()) {
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

        userDetailService.addBlackList(userId.toString(), user.getName(), "유저 정보 수정");
    }

    @Transactional
    public void withdrawal(String userId) {
        User user = userRepository.findByIdAndStatus(Long.valueOf(userId), UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        user.withdrawal();

        userDetailService.addBlackList(userId, user.getName(), "유저 탈퇴");
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(String userId) {
        User user = userRepository.findByIdAndStatus(Long.valueOf(userId), UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        return UserResponse.from(user);
    }
}
