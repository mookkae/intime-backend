package com.intime.domain.trade;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TradePostCode implements BaseCode {

    TRADE_POST_NOT_FOUND("TRADE_POST_NOT_FOUND", HttpStatus.NOT_FOUND, "판매 게시글을 찾을 수 없습니다."),
    TRADE_POST_NOT_OWNER("TRADE_POST_NOT_OWNER", HttpStatus.FORBIDDEN, "본인의 판매 게시글만 수정할 수 있습니다."),
    TRADE_POST_DUPLICATE("TRADE_POST_DUPLICATE", HttpStatus.CONFLICT, "이미 판매 등록된 대기표입니다."),
    TRADE_POST_INVALID_STATE("TRADE_POST_INVALID_STATE", HttpStatus.BAD_REQUEST, "현재 상태에서 수행할 수 없는 작업입니다."),
    TICKET_NOT_TRADEABLE("TICKET_NOT_TRADEABLE", HttpStatus.BAD_REQUEST, "WAITING 상태의 대기표만 판매 등록할 수 있습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
