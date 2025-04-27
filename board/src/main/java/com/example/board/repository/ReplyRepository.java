package com.example.board.repository;

import com.example.board.domain.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Optional<Reply> findByIdAndUserId(long id, long userId);

    @Modifying
    @Query("UPDATE Reply r" +
            " SET r.nickname = :nickname" +
            " WHERE r.userId = :userId")
    void bulkUpdateNicknameByUserId(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );
}
