package com.example.board.controller;

import com.example.board.dto.request.ReplyCreateRequest;
import com.example.board.dto.request.ReplyUpdateRequest;
import com.example.board.dto.response.GlobalResponse;
import com.example.board.dto.response.ReplyResponse;
import com.example.board.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/replies")
public class ReplyController {

    private final ReplyService replyService;

    @Operation(summary = "댓글 작성", description = "reply 를 생성합니다.")
    @PostMapping("")
    public ResponseEntity<GlobalResponse<Object>> create(
            @PathVariable Long postId,
            @Valid @RequestBody ReplyCreateRequest request
    ) {
        replyService.create(postId, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "댓글 수정", description = "reply 를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> update(
            @PathVariable Long postId,
            @PathVariable Long id,
            @Valid @RequestBody ReplyUpdateRequest request
    ) {
        replyService.update(postId, id, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "댓글 삭제", description = "reply 를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> delete(
            @PathVariable Long postId,
            @PathVariable Long id
    ) {
        replyService.delete(postId, id);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "댓글 조회", description = "해당 posts의 reply 를 조회합니다. 시간순 정렬 + 순차적 로딩이므로 효율을 위해 커서 기반 페이징 적용")
    @GetMapping("")
    public ResponseEntity<GlobalResponse<List<ReplyResponse>>> getList(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(GlobalResponse.of(replyService.getList(postId, cursor, size)));
    }
}
