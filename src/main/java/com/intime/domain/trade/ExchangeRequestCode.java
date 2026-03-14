package com.intime.domain.trade;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExchangeRequestCode implements BaseCode {

    EXCHANGE_REQUEST_NOT_FOUND("EXCHANGE_REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND, "교환 신청을 찾을 수 없습니다."),
    EXCHANGE_REQUEST_NOT_OWNER("EXCHANGE_REQUEST_NOT_OWNER", HttpStatus.FORBIDDEN, "본인의 교환 신청만 취소할 수 있습니다."),
    EXCHANGE_REQUEST_INVALID_STATE("EXCHANGE_REQUEST_INVALID_STATE", HttpStatus.BAD_REQUEST, "현재 상태에서 수행할 수 없는 작업입니다."),
    EXCHANGE_REQUEST_SELF_POST("EXCHANGE_REQUEST_SELF_POST", HttpStatus.BAD_REQUEST, "본인의 판매 게시글에 신청할 수 없습니다."),
    BUYER_TICKET_NOT_WAITING("BUYER_TICKET_NOT_WAITING", HttpStatus.BAD_REQUEST, "WAITING 상태의 대기표로만 교환 신청할 수 있습니다."),
    BUYER_TICKET_NOT_OWNER("BUYER_TICKET_NOT_OWNER", HttpStatus.FORBIDDEN, "본인의 대기표로만 교환 신청할 수 있습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
