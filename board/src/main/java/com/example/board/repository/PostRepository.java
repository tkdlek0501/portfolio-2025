package com.example.board.repository;

import com.example.board.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByPostCategoryId(Long postCategoryId, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p" +
            " SET p.nickname = :nickname" +
            " WHERE p.userId = :userId")
    void bulkUpdateNicknameByUserId(
            @Param("userId") Long userId,
            @Param("nickname") String nickname
    );
}
