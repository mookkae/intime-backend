package com.intime.application.waiting;

import com.intime.application.trade.TradeLifecycleService;
import com.intime.domain.waiting.WaitingTicket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WaitingBatchProcessor {

    private final TradeLifecycleService tradeLifecycleService;
    private final WaitingEventPublisher waitingEventPublisher;
    private final Clock clock;

    @Transactional
    public void processPendingCall(WaitingTicket ticket) {
        tradeLifecycleService.cancelActiveNegotiationByTicket(ticket.getId());
        ticket.clearPendingCall();
        ticket.call(LocalDateTime.now(clock));
        waitingEventPublisher.publishCalled(ticket.getId());
    }

    @Transactional
    public void processNoShow(WaitingTicket ticket) {
        tradeLifecycleService.cancelActiveNegotiationByTicket(ticket.getId());
        ticket.noShow();
    }
}
