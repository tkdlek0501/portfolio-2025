package com.example.user.service;

import com.example.user.domain.entity.User;
import com.example.user.dto.request.UserCreateRequest;
import com.example.user.exception.AlreadyExistsUserException;
import com.example.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

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
}
