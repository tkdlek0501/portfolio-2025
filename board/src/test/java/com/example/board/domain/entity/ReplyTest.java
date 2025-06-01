package com.example.board.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ReplyTest {

    @DisplayName("댓글 생성 성공 테스트")
    @Test
    void create_성공() {
        long userId = 1L;
        long postId = 10L;
        String nickname = "tester";
        String content = "reply";

        Reply reply = Reply.create(userId, postId, nickname, content);

        assertThat(reply).isNotNull();
        assertThat(reply.getUserId()).isEqualTo(userId);
        assertThat(reply.getPostId()).isEqualTo(postId);
        assertThat(reply.getNickname()).isEqualTo(nickname);
        assertThat(reply.getContent()).isEqualTo(content);
    }

    @DisplayName("댓글 수정 성공 테스트")
    @Test
    void modify_성공() {
        Reply reply = Reply.create(1L, 10L, "tester", "old content");

        reply.modify("new content");

        assertThat(reply.getContent()).isEqualTo("new content");
    }
}
