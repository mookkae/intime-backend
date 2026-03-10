package com.intime.common.advice;

import com.intime.common.ApiResponse;
import com.intime.common.CommonCode;
import com.intime.common.exception.BaseCustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalAdvice {

    @ExceptionHandler(BaseCustomException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseCustomException(BaseCustomException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage(), e);

        return ResponseEntity
                .status(e.getBaseCode().getStatus())
                .body(ApiResponse.error(e.getBaseCode()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());

        return ResponseEntity
                .status(CommonCode.DUPLICATE_RESOURCE.getStatus())
                .body(ApiResponse.error(CommonCode.DUPLICATE_RESOURCE));
    }
}
