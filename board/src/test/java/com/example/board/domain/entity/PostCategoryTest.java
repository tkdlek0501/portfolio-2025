package com.example.board.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PostCategoryTest {

    @DisplayName("게시글 카테고리 생성 성공 테스트")
    @Test
    void create_성공() {
        String name = "Development";

        PostCategory category = PostCategory.create(name);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo(name);
    }

    @DisplayName("게시글 카테고리 수정 성공 테스트")
    @Test
    void modify_성공() {
        PostCategory category = PostCategory.create("OldName");

        category.modify("NewName");

        assertThat(category.getName()).isEqualTo("NewName");
    }
}
