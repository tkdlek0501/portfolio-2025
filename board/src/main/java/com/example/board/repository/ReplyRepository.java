package com.example.board.repository;

import com.example.board.domain.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Optional<Reply> findByIdAndUserId(long id, long userId);
}
