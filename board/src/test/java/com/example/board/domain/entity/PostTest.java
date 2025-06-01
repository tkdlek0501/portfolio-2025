package com.example.board.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PostTest {

    @DisplayName("게시글 생성 성공 테스트")
    @Test
    void create_성공() {
        long userId = 1L;
        String nickname = "user1";
        long categoryId = 10L;
        String title = "Test Title";
        String content = "test content";

        Post post = Post.create(userId, nickname, categoryId, title, content);

        assertThat(post).isNotNull();
        assertThat(post.getUserId()).isEqualTo(userId);
        assertThat(post.getNickname()).isEqualTo(nickname);
        assertThat(post.getPostCategoryId()).isEqualTo(categoryId);
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getLikeCount()).isEqualTo(0);
        assertThat(post.getViewCount()).isEqualTo(0);
    }

    @DisplayName("게시글 수정 성공 테스트")
    @Test
    void modify_성공() {
        Post post = Post.create(1L, "nick", 10L, "Old Title", "old content");

        long newCategoryId = 20L;
        String newTitle = "New Title";
        String newContent = "new content";

        post.modify(newCategoryId, newTitle, newContent);

        assertThat(post.getPostCategoryId()).isEqualTo(newCategoryId);
        assertThat(post.getTitle()).isEqualTo(newTitle);
        assertThat(post.getContent()).isEqualTo(newContent);
    }

    @DisplayName("게시글 좋아요 증가 성공 테스트")
    @Test
    void increaseLikeCount_성공() {
        Post post = Post.create(1L, "nick", 10L, "Title", "Content");

        int before = post.getLikeCount();
        post.increaseLikeCount();

        assertThat(post.getLikeCount()).isEqualTo(before + 1);
    }

    @DisplayName("게시글 좋아요 감소 성공 테스트")
    @Test
    void decreaseLikeCount_성공() {
        Post post = Post.create(1L, "nick", 10L, "Title", "Content");
        post.increaseLikeCount(); // likeCount = 1

        int before = post.getLikeCount();
        post.decreaseLikeCount();

        assertThat(post.getLikeCount()).isEqualTo(before - 1);
    }

    @DisplayName("게시글 조회 증가 성공 테스트")
    @Test
    void increaseViewCount_성공() {
        Post post = Post.create(1L, "nick", 10L, "Title", "Content");

        int before = post.getViewCount();
        post.increaseViewCount();

        assertThat(post.getViewCount()).isEqualTo(before + 1);
    }
}
