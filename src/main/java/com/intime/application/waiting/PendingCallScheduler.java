package com.intime.application.waiting;

import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingCallScheduler {

    private static final int PENDING_CALL_TIMEOUT_MINUTES = 5;

    private final WaitingTicketRepository waitingTicketRepository;
    private final WaitingBatchProcessor waitingBatchProcessor;
    private final Clock clock;

    @Scheduled(fixedDelay = 30_000)
    public void processPendingCallExpiry() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(PENDING_CALL_TIMEOUT_MINUTES);
        List<WaitingTicket> expired = waitingTicketRepository
                .findByPendingCallAtBeforeAndStatus(threshold, WaitingStatus.WAITING);

        for (WaitingTicket ticket : expired) {
            try {
                waitingBatchProcessor.processPendingCall(ticket);
            } catch (Exception e) {
                log.warn("보류 호출 처리 실패 - ticketId: {}", ticket.getId(), e);
            }
        }
    }
}
