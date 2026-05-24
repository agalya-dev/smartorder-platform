package com.smartorder.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private String status;
    private int code;
    private String message;
    private String timestamp;
    private T data;
    private List<ErrorDetail> errors;

    // Success response
    public static <T> ApiResponse<T> success(T data, String message, int code) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .data(data)
                .build();
    }

    // Error response
    public static <T> ApiResponse<T> error(String status, int code,
                                           String message, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .status(status)
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .errors(errors)
                .build();
    }
}