package com.example.board.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ViewTest {

    @Test
    void create_성공() {
        long userId = 1L;
        long postId = 1L;

        View view = View.create(userId, postId);

        assertThat(view).isNotNull();
        assertThat(view.getUserId()).isEqualTo(userId);
        assertThat(view.getPostId()).isEqualTo(postId);
    }
}
