package com.example.user.repository;

import com.example.user.domain.entity.User;
import com.example.user.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findByNameAndIdNot(String name, Long id);

    Optional<User> findByIdAndStatus(Long id, UserStatus status);

    Optional<User> findByNameAndStatus(String name, UserStatus status);
}
