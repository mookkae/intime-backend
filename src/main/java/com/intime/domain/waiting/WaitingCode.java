package com.intime.domain.waiting;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WaitingCode implements BaseCode {

    WAITING_CREATED("WAITING_CREATED_201", HttpStatus.CREATED, "웨이팅이 등록되었습니다."),
    WAITING_FOUND("WAITING_FOUND_200", HttpStatus.OK, "웨이팅 조회에 성공했습니다."),
    WAITING_CANCELLED("WAITING_CANCELLED_200", HttpStatus.OK, "웨이팅이 취소되었습니다."),
    WAITING_CALLED("WAITING_CALLED_200", HttpStatus.OK, "다음 순번이 호출되었습니다."),
    WAITING_SEATED("WAITING_SEATED_200", HttpStatus.OK, "착석 처리되었습니다."),
    WAITING_NO_SHOW("WAITING_NO_SHOW_200", HttpStatus.OK, "노쇼 처리되었습니다."),
    WAITING_NOT_FOUND("WAITING_NOT_FOUND_404", HttpStatus.NOT_FOUND, "대기표를 찾을 수 없습니다."),
    WAITING_INVALID_STATE("WAITING_INVALID_STATE_400", HttpStatus.BAD_REQUEST, "현재 상태에서 수행할 수 없는 작업입니다."),
    WAITING_NOT_OWNER("WAITING_NOT_OWNER_403", HttpStatus.FORBIDDEN, "본인의 대기표만 취소할 수 있습니다."),
    WAITING_NO_ONE_WAITING("WAITING_NO_ONE_WAITING_404", HttpStatus.NOT_FOUND, "대기 중인 순번이 없습니다."),
    WAITING_REGISTER_FAILED("WAITING_REGISTER_FAILED_409", HttpStatus.CONFLICT, "웨이팅 등록에 실패했습니다. 다시 시도해주세요.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
