package com.intime.domain.waiting;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WaitingCode implements BaseCode {

    WAITING_NOT_FOUND("WAITING_NOT_FOUND", HttpStatus.NOT_FOUND, "대기표를 찾을 수 없습니다."),
    WAITING_INVALID_STATE("WAITING_INVALID_STATE", HttpStatus.BAD_REQUEST, "현재 상태에서 수행할 수 없는 작업입니다."),
    WAITING_NOT_OWNER("WAITING_NOT_OWNER", HttpStatus.FORBIDDEN, "본인의 대기표만 취소할 수 있습니다."),
    WAITING_NO_ONE_WAITING("WAITING_NO_ONE_WAITING", HttpStatus.NOT_FOUND, "대기 중인 순번이 없습니다."),
    WAITING_REGISTER_FAILED("WAITING_REGISTER_FAILED", HttpStatus.CONFLICT, "웨이팅 등록에 실패했습니다. 다시 시도해주세요."),
    WAITING_DUPLICATE("WAITING_DUPLICATE", HttpStatus.CONFLICT, "이미 웨이팅 중인 가게입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
