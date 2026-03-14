package com.intime.domain.store;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreCode implements BaseCode {

    STORE_NOT_FOUND("STORE_NOT_FOUND", HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
