package com.example.board.repository;

import com.example.board.domain.entity.View;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewRepository extends JpaRepository<View, Long> {

    Optional<View> findByPostIdAndUserId(Long postId, Long userId);
}
