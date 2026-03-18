package com.intime.application.waiting;

import com.intime.application.waiting.dto.WaitingPositionInfo;
import com.intime.application.waiting.dto.WaitingTicketInfo;

import java.util.List;

public interface WaitingQueryService {

    List<WaitingTicketInfo> getStoreQueue(Long storeId);

    List<WaitingTicketInfo> getMyTickets(Long memberId);

    WaitingPositionInfo getMyPosition(Long ticketId);
}
