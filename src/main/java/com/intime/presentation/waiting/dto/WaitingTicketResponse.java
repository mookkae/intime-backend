package com.intime.presentation.waiting.dto;

import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;

import java.time.LocalDate;

public record WaitingTicketResponse(
        Long id,
        Long storeId,
        Long memberId,
        int positionNumber,
        WaitingStatus status,
        int partySize,
        LocalDate waitingDate,
        TradePostInfo tradePost
) {

    public record TradePostInfo(Long tradePostId, String description) {}

    public static WaitingTicketResponse from(WaitingTicket ticket) {
        return from(ticket, null);
    }

    public static WaitingTicketResponse from(WaitingTicket ticket, TradePostInfo tradePost) {
        return new WaitingTicketResponse(
                ticket.getId(),
                ticket.getStoreId(),
                ticket.getMemberId(),
                ticket.getPositionNumber(),
                ticket.getStatus(),
                ticket.getPartySize(),
                ticket.getWaitingDate(),
                tradePost
        );
    }
}
