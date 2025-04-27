package com.example.board.repository;

import com.example.board.domain.entity.ChildReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChildReplyRepository extends JpaRepository<ChildReply, Long> {

    Optional<ChildReply> findByIdAndUserId(long id, long userId);

    @Modifying
    @Query("UPDATE ChildReply cr" +
            " SET cr.nickname = :nickname" +
            " WHERE cr.userId = :userId")
    void bulkUpdateNicknameByUserId(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );
}
