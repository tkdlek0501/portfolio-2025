package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserStatus;
import com.example.user.dto.event.UserUpdatedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailService userDetailService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // 단 건 저장에서는 굳이 transactional 어노테이션 사용 x, 트랜잭션 범위 최소화
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
    public void update(UserUpdateRequest request, String jwt) {

        long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        if (!Objects.equals(user.getName(), request.name())) {
            // 본 user 는 제외
            if (userRepository.findByNameAndIdNot(request.name(), userId).isPresent()) {
                throw new AlreadyExistsUserException();
            }
        }

        boolean isEqualsNickname = user.isEqualsNickname(request.nickname());

        user.modify(passwordEncoder,
                request.name(),
                request.password(),
                request.nickname(),
                request.phone(),
                request.email()
        );

        // 블랙리스트 추가
        userDetailService.addBlackList(userId, "유저 정보 수정", jwt);

        // board 서비스와 데이터 정합성 유지를 위한 동기화
        // 유저 정보 수정시 아웃박스 테이블에 PENDING 상태로 데이터 저장 + 카프카 메시지 발행 (각각 다른 리스너로 처리)
        if (!isEqualsNickname) {
            UserUpdatedEvent event = UserUpdatedEvent.of(UUID.randomUUID(), userId, user.getNickname());
            eventPublisher.publishEvent(event);
        }
    }

    @Transactional
    public void withdrawal(String jwt) {
        long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        user.withdrawal();

        userDetailService.addBlackList(userId, "유저 탈퇴", jwt);
    }

    @Transactional(readOnly = true)
    public UserResponse getMe() {
        long userId = JwtUtil.getId();
        User user = userRepository.findByIdAndStatus(userId, UserStatus.NORMAL)
                .orElseThrow(() -> new ResourceNotFoundException("user"));

        return UserResponse.from(user);
    }
}
