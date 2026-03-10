package com.intime.domain.store;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreCode implements BaseCode {

    STORE_CREATED("STORE_CREATED_201", HttpStatus.CREATED, "가게가 등록되었습니다."),
    STORE_FOUND("STORE_FOUND_200", HttpStatus.OK, "가게 조회에 성공했습니다."),
    STORE_NOT_FOUND("STORE_NOT_FOUND_404", HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
