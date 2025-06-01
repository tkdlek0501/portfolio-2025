package com.example.board.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LikeTest {

    @DisplayName("좋아요 생성 성공 테스트")
    @Test
    void create_성공() {
        long userId = 1L;
        long postId = 100L;

        Like like = Like.create(userId, postId);

        assertThat(like).isNotNull();
        assertThat(like.getUserId()).isEqualTo(userId);
        assertThat(like.getPostId()).isEqualTo(postId);
    }
}
