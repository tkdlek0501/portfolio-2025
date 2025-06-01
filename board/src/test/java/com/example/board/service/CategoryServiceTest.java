package com.example.board.service;

import com.example.board.domain.entity.PostCategory;
import com.example.board.dto.request.CategoryCreateRequest;
import com.example.board.dto.request.CategoryUpdateRequest;
import com.example.board.exception.NotAllowedPostCategoryException;
import com.example.board.exception.ResourceNotFoundException;
import com.example.board.repository.PostCategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private PostCategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @DisplayName("카테고리 생성 성공")
    @Test
    void create_성공() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("자유");
        when(categoryRepository.findByName("자유")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(mock(PostCategory.class));

        // when
        categoryService.create(request);

        // then
        verify(categoryRepository, times(1)).save(any(PostCategory.class));
    }

    @DisplayName("이미 존재하는 이름으로 카테고리 생성 시 예외 발생")
    @Test
    void create_이미존재하는이름으로_카테고리생성시_예외반생() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("자유");
        when(categoryRepository.findByName("자유")).thenReturn(Optional.of(mock(PostCategory.class)));

        // when & then
        assertThrows(NotAllowedPostCategoryException.class, () -> categoryService.create(request));
        verify(categoryRepository, never()).save(any());
    }

    @DisplayName("카테고리 수정 성공")
    @Test
    void update_성공() {
        // given
        long id = 1L;
        CategoryUpdateRequest request = new CategoryUpdateRequest("공지사항");
        PostCategory postCategory = mock(PostCategory.class);

        when(categoryRepository.findById(id)).thenReturn(Optional.of(postCategory));
        doNothing().when(postCategory).modify("공지사항");

        // when
        categoryService.update(id, request);

        // then
        verify(postCategory).modify("공지사항");
    }

    @DisplayName("카테고리 수정 실패 - 존재하지 않음")
    @Test
    void update_없는_카테고리_수정시_예외반생 () {
        // given
        long id = 999L;
        CategoryUpdateRequest request = new CategoryUpdateRequest("공지");

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> categoryService.update(id, request));
    }

    @DisplayName("카테고리 삭제 성공")
    @Test
    void delete_성공() {
        // given
        long id = 1L;
        PostCategory postCategory = mock(PostCategory.class);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(postCategory));
        doNothing().when(categoryRepository).delete(postCategory);

        // when
        categoryService.delete(id);

        // then
        verify(categoryRepository).delete(postCategory);
    }

    @DisplayName("카테고리 삭제 실패 - 존재하지 않음")
    @Test
    void delete_없는_카테고리_삭제시_예외반생 () {
        // given
        long id = 1L;
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class, () -> categoryService.delete(id));
        verify(categoryRepository, never()).delete(any());
    }

    @DisplayName("모든 카테고리 조회")
    @Test
    void findAll_성공() {
        // given
        PostCategory mockCategory1 = mock(PostCategory.class);
        when(mockCategory1.getName()).thenReturn("공지사항");

        PostCategory mockCategory2 = mock(PostCategory.class);
        when(mockCategory2.getName()).thenReturn("자유");

        List<PostCategory> mockList = Arrays.asList(mockCategory1, mockCategory2);
        when(categoryRepository.findAll()).thenReturn(mockList);

        // when
        List<PostCategory> result = categoryService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").contains("공지사항", "자유");
    }
}
