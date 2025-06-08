package com.example.board.controller;

import com.example.board.dto.request.CategoryCreateRequest;
import com.example.board.dto.request.CategoryUpdateRequest;
import com.example.board.dto.response.GlobalResponse;
import com.example.board.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 생성", description = "postCategory 를 생성합니다.")
    @PostMapping("")
    public ResponseEntity<GlobalResponse<Object>> create(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        categoryService.create(request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "카테고리 수정", description = "postCategory 를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        categoryService.update(id, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "카테고리 삭제", description = "postCategory 를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "카테고리 조회", description = "postCategory 를 조회합니다.")
    @GetMapping("")
    public ResponseEntity<GlobalResponse<Object>> findAll() {
        return ResponseEntity.ok(GlobalResponse.of(categoryService.findAll()));
    }
}
