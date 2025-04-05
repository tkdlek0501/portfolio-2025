package com.example.user.dto.response;

import com.example.user.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Collections;

@Builder
public record GlobalResponse<T>(
        @Schema(description = "응답 상태 (1: 성공, -1: 실패)", example = "1")
        short status,
        @Schema(description = "응답 코드", example = "OK")
        String code,
        @Schema(description = "응답 메시지", example = "정상 처리되었습니다.")
        String message,
        @Schema(description = "응답 데이터", nullable = true)
        T data
)
{

    private static final short STATUS_SUCCESS = 1;
    private static final short STATUS_FAILURE = -1;

    /**
     * 데이터 없이 성공 응답
     * data는 빈 Map 으로 초기화되어 프론트에서 항상 "data": {} 형태로 보이게 함
     */
    public static GlobalResponse<Object> of() {
        return GlobalResponse.builder()
                .status(STATUS_SUCCESS)
                .code("OK")
                .message("성공")
                .data(Collections.emptyMap())
                .build();
    }

    /**
     * 데이터 포함한 성공 응답
     */
    public static <T> GlobalResponse<T> of(T data) {
        return GlobalResponse.<T>builder()
                .status(STATUS_SUCCESS)
                .code("OK")
                .message("성공")
                .data(data != null ? data : (T) Collections.emptyMap())
                .build();
    }

    /**
     * GlobalException 처리용 응답
     */
    public static GlobalResponse<Object> of(GlobalException e) {
        return GlobalResponse.builder()
                .status(STATUS_FAILURE)
                .code(e.getCode())
                .message(e.getMessage())
                .data(Collections.emptyMap())
                .build();
    }

    /**
     * 일반 Exception 처리용 응답
     */
    public static GlobalResponse<Object> of(Exception e) {
        return GlobalResponse.builder()
                .status(STATUS_FAILURE)
                .code("SYS500")
                .message("예상하지 못한 에러가 발생했습니다.")
                .data(Collections.emptyMap())
                .build();
    }
}
