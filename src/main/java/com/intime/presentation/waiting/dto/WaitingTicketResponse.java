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
        LocalDate waitingDate
) {

    public static WaitingTicketResponse from(WaitingTicket ticket) {
        return new WaitingTicketResponse(
                ticket.getId(),
                ticket.getStoreId(),
                ticket.getMemberId(),
                ticket.getPositionNumber(),
                ticket.getStatus(),
                ticket.getPartySize(),
                ticket.getWaitingDate()
        );
    }
}
