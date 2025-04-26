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
@RequestMapping("/replies/{replyId}/child-replies")
public class ChildReplyController {

    private final ReplyService replyService;

    @Operation(summary = "대댓글 작성", description = "childReply 를 생성합니다.")
    @PostMapping("")
    public ResponseEntity<GlobalResponse<Object>> create(
            @PathVariable Long replyId,
            @Valid @RequestBody ReplyCreateRequest request
    ) {
        replyService.createChild(replyId, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "대댓글 수정", description = "childReply 를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> update(
            @PathVariable Long replyId,
            @PathVariable Long id,
            @Valid @RequestBody ReplyUpdateRequest request
    ) {
        replyService.updateChild(replyId, id, request);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "대댓글 삭제", description = "childReply 를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<GlobalResponse<Object>> delete(
            @PathVariable Long replyId,
            @PathVariable Long id
    ) {
        replyService.deleteChild(replyId, id);
        return ResponseEntity.ok(GlobalResponse.of());
    }

    @Operation(summary = "대댓글 조회", description = "해당 reply의 childReply 를 조회합니다. 시간순 정렬 + 순차적 로딩이므로 효율을 위해 커서 기반 페이징 적용")
    @GetMapping("")
    public ResponseEntity<GlobalResponse<List<ReplyResponse>>> getList(
            @PathVariable Long replyId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(GlobalResponse.of(replyService.getChildList(replyId, cursor, size)));
    }
}
