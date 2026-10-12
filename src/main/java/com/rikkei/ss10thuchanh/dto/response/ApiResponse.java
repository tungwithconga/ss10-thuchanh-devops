package com.rikkei.ss10thuchanh.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status(201)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
