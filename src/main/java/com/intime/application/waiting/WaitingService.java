package com.intime.application.waiting;

import com.intime.application.waiting.dto.WaitingRegisterCommand;
import com.intime.application.waiting.dto.WaitingTicketInfo;

public interface WaitingService {

    WaitingTicketInfo register(WaitingRegisterCommand command);

    void cancel(Long ticketId, Long memberId);

    WaitingTicketInfo callNext(Long storeId);

    void confirmSeated(Long ticketId);

    void markNoShow(Long ticketId);
}
