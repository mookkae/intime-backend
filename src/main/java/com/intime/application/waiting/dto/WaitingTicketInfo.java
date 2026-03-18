package com.intime.application.waiting.dto;

import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;

import java.time.LocalDate;

public record WaitingTicketInfo(
        Long id,
        Long storeId,
        Long memberId,
        int positionNumber,
        WaitingStatus status,
        int partySize,
        LocalDate waitingDate,
        TradePostSummary tradePost
) {

    public record TradePostSummary(Long tradePostId, Long price) {
    }

    public static WaitingTicketInfo from(WaitingTicket ticket) {
        return from(ticket, null);
    }

    public static WaitingTicketInfo from(WaitingTicket ticket, TradePostSummary tradePost) {
        return new WaitingTicketInfo(
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
