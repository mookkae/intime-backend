package com.intime.common.advice;

import com.intime.common.CommonCode;
import com.intime.common.ErrorResponse;
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
    public ResponseEntity<ErrorResponse> handleBaseCustomException(BaseCustomException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getBaseCode().getStatus())
                .body(new ErrorResponse(e.getBaseCode().getCode(), e.getBaseCode().getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("데이터 무결성 위반: {}", e.getMessage());
        return ResponseEntity
                .status(CommonCode.DUPLICATE_RESOURCE.getStatus())
                .body(new ErrorResponse(CommonCode.DUPLICATE_RESOURCE.getCode(), CommonCode.DUPLICATE_RESOURCE.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(CommonCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ErrorResponse(CommonCode.INTERNAL_SERVER_ERROR.getCode(), CommonCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
