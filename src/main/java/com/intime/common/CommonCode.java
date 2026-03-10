package com.intime.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonCode implements BaseCode {

    SUCCESS("SUCCESS_200", HttpStatus.OK, "요청이 정상적으로 처리되었습니다."),
    INVALID_INPUT("INVALID_INPUT_400", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE_409", HttpStatus.CONFLICT, "중복된 데이터입니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR_500", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
