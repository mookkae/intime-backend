package com.intime.application.waiting;

import com.intime.domain.waiting.WaitingTicket;

public interface WaitingService {

    WaitingTicket register(Long storeId, Long memberId, int partySize);

    void cancel(Long ticketId, Long memberId);

    WaitingTicket callNext(Long storeId);

    void confirmSeated(Long ticketId);

    void markNoShow(Long ticketId);
}
