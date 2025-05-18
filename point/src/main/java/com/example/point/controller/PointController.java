package com.example.point.controller;

import com.example.point.dto.response.GlobalResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/points")
public class PointController {

    @Operation(summary = "포인트 적립", description = "포인트를 적립합니다.")
    @GetMapping("/test")
    public ResponseEntity<GlobalResponse<Object>> test() {
        return ResponseEntity.ok(GlobalResponse.of());
    }
}
