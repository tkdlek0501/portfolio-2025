package com.example.board.controller;

import com.example.board.dto.request.PostCreateRequest;
import com.example.board.dto.request.PostUpdateRequest;
import com.example.board.dto.request.ReplyCreateRequest;
import com.example.board.dto.response.GlobalResponse;
import com.example.board.dto.response.PostResponse;
import com.example.board.dto.response.PostWrapResponse;
import com.example.board.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "post 를 생성합니다.")
    @PostMapping("")
    public ResponseEntity<GlobalResponse<Object>> create(
            @Valid @RequestBody PostCreateRequest request
    ) {
        postService.create(request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "게시글 수정", description = "post를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.update(id, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "게시글 목록 조회", description = "해당 카테고리의 게시글 목록을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<GlobalResponse<PostWrapResponse>> findAllByCategory(
            @RequestParam("categoryId") Long categoryId,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(GlobalResponse.of(postService.findAllByCategoryId(categoryId, pageable)));
    }

    @Operation(summary = "게시글 상세 조회", description = "해당 id의 게시글을 상세 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<GlobalResponse<PostResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(GlobalResponse.of(postService.findById(id)));
    }

    @Operation(summary = "게시글 좋아요", description = "해당 id의 게시글의 like를 생성하고 like count를 1 증가 시킵니다.")
    @PostMapping("/{id}/like")
    public ResponseEntity<GlobalResponse<Object>> createLike(@PathVariable Long id) {
        postService.createLike(id);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "게시글 좋아요 취소", description = "해당 id 게시글에 생성된 like를 삭제하고 like count를 1 감소 시킵니다.")
    @DeleteMapping("/{id}/like")
    public ResponseEntity<GlobalResponse<Object>> deleteLike(@PathVariable Long id) {
        postService.deleteLike(id);
        return ResponseEntity.ok(GlobalResponse.of());
    }
}
