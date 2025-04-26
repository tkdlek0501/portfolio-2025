package com.example.board.service;

import com.example.board.domain.entity.PostCategory;
import com.example.board.dto.request.CategoryCreateRequest;
import com.example.board.dto.request.CategoryUpdateRequest;
import com.example.board.exception.NotAllowedPostCategoryException;
import com.example.board.exception.ResourceNotFoundException;
import com.example.board.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final PostCategoryRepository categoryRepository;

    public void create(CategoryCreateRequest request) {
        PostCategory postCategory = categoryRepository.findByName(request.name())
                .orElse(null);
        if (postCategory != null) throw new NotAllowedPostCategoryException();

        categoryRepository.save(PostCategory.create(request.name()));
    }

    @Transactional
    public void update(long id, CategoryUpdateRequest request) {
        PostCategory postCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("postCategory"));

        postCategory.modify(request.name());
    }

    public void delete(Long id) {
        PostCategory postCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("postCategory"));
        categoryRepository.delete(postCategory);
    }

    @Transactional(readOnly = true)
    public List<PostCategory> findAll() {
        return categoryRepository.findAll();
    }
}
