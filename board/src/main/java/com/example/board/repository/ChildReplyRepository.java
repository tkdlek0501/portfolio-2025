package com.example.board.repository;

import com.example.board.domain.entity.ChildReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChildReplyRepository extends JpaRepository<ChildReply, Long> {

    Optional<ChildReply> findByIdAndUserId(long id, long userId);
}
