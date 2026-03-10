package com.intime.support.fixture;

import com.intime.domain.waiting.WaitingTicket;
import com.intime.support.TestReflectionUtils;

import java.time.LocalDate;

public class WaitingTicketFixture {

    public static WaitingTicket createTicket() {
        return createTicket(1L, 1L, 1L, 1, 2);
    }

    public static WaitingTicket createTicket(Long ticketId, Long storeId, Long memberId, int positionNumber, int partySize) {
        WaitingTicket ticket = WaitingTicket.create(storeId, memberId, positionNumber, partySize, LocalDate.of(2026, 3, 11));
        if (ticketId != null) {
            TestReflectionUtils.setField(ticket, "id", ticketId);
        }
        return ticket;
    }
}
