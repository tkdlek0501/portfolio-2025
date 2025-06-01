package com.example.board.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ChildReplyTest {

    @DisplayName("대댓글 생성 성공 테스트")
    @Test
    void create_성공() {
        long userId = 1L;
        long replyId = 1L;
        String nickname = "childTester";
        String content = "child reply";

        ChildReply childReply = ChildReply.create(userId, replyId, nickname, content);

        assertThat(childReply).isNotNull();
        assertThat(childReply.getUserId()).isEqualTo(userId);
        assertThat(childReply.getReplyId()).isEqualTo(replyId);
        assertThat(childReply.getNickname()).isEqualTo(nickname);
        assertThat(childReply.getContent()).isEqualTo(content);
    }

    @DisplayName("대댓글 수정 성공 테스트")
    @Test
    void modify_성공() {
        ChildReply childReply = ChildReply.create(1L, 1L, "tester", "old content");

        childReply.modify("new content");

        assertThat(childReply.getContent()).isEqualTo("new content");
    }
}
