package com.example.board.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_category_id", nullable = false)
    private Long postCategoryId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "name", length = 100, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "created_date", nullable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "updated_date", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedDate;

    public static Post create(
            long userId,
            String nickname,
            long postCategoryId,
            String title,
            String content
    ) {
        return Post.builder()
                .userId(userId)
                .nickname(nickname)
                .postCategoryId(postCategoryId)
                .title(title)
                .content(content)
                .build();
    }

    public void modify(
            long postCategoryId,
            String title,
            String content
    ) {
        this.postCategoryId = postCategoryId;
        this.title = title;
        this.content = content;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount--;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
