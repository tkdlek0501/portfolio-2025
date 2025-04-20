package com.example.board.repository;

import com.example.board.domain.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByPostCategoryId(Long postCategoryId, Pageable pageable);
}
