package com.intime.application.waiting;

import com.intime.domain.waiting.WaitingTicket;

import java.util.List;

public interface WaitingQueryService {

    List<WaitingTicket> getStoreQueue(Long storeId);

    List<WaitingTicket> getMyTickets(Long memberId);

    WaitingPositionResponse getMyPosition(Long ticketId);
}
