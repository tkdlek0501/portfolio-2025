package com.example.user.dto.response;

import com.example.user.exception.GlobalException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class GlobalResponse {

    private static final short STATUS_SUCCESS = 1;
    private static final short STATUS_FAILURE = -1;

    private final short status;
    private final String code;

    private String message;
    private Object data;

    public static GlobalResponse of() {
        return GlobalResponse.builder()
                .status(STATUS_SUCCESS)
                .code("OK")
                .build();
    }

    public static GlobalResponse of(Object data) {
        return GlobalResponse.builder()
                .status(STATUS_SUCCESS)
                .code("OK")
                .data(data)
                .build();
    }

    public static GlobalResponse of(GlobalException e) {
        return GlobalResponse.builder()
                .status(STATUS_FAILURE)
                .code(e.getCode())
                .message(e.getMessage())
                .build();
    }

    public static GlobalResponse of(Exception e) {
        return GlobalResponse.builder()
                .status(STATUS_FAILURE)
                .code("SYS500")
                .message("예상하지 못한 에러가 발생했습니다.")
                .build();
    }
}
