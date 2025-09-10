package com.domaindns.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getAllErrors().stream().findFirst().map(e -> e.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.error(40001, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArg(IllegalArgumentException ex) {
        return ApiResponse.error(40001, ex.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    public ApiResponse<Void> handleSecurity(SecurityException ex) {
        return ApiResponse.error(40101, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception ex) {
        return ApiResponse.error(50000,
                ex.getMessage() != null ? ex.getMessage() : HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }
}
