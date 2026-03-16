package com.intime.domain.negotiation;

import com.intime.common.BaseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NegotiationCode implements BaseCode {

    NEGOTIATION_NOT_FOUND("NEGOTIATION_NOT_FOUND", HttpStatus.NOT_FOUND, "협상을 찾을 수 없습니다."),
    NOT_YOUR_TURN("NOT_YOUR_TURN", HttpStatus.BAD_REQUEST, "상대방의 차례입니다."),
    SELF_ACCEPT("SELF_ACCEPT", HttpStatus.BAD_REQUEST, "본인의 오퍼는 수락할 수 없습니다."),
    NEGOTIATION_INVALID_STATE("NEGOTIATION_INVALID_STATE", HttpStatus.BAD_REQUEST, "현재 상태에서 수행할 수 없는 작업입니다."),
    NEGOTIATION_NOT_PARTICIPANT("NEGOTIATION_NOT_PARTICIPANT", HttpStatus.FORBIDDEN, "본인이 참여한 협상만 처리할 수 있습니다."),
    SELLER_TICKET_NOT_TRADEABLE("SELLER_TICKET_NOT_TRADEABLE", HttpStatus.BAD_REQUEST, "판매자 대기표가 거래 불가 상태입니다."),
    ALREADY_SUBMITTED_FINAL_OFFER("ALREADY_SUBMITTED_FINAL_OFFER", HttpStatus.BAD_REQUEST, "이미 최종 가격을 제출했습니다."),
    BUYER_TICKET_NOT_TRADEABLE("BUYER_TICKET_NOT_TRADEABLE", HttpStatus.BAD_REQUEST, "구매자 대기표가 거래 불가 상태입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
