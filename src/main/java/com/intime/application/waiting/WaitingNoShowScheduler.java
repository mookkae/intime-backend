package com.intime.application.waiting;

import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitingNoShowScheduler {

    private static final int NO_SHOW_TIMEOUT_MINUTES = 5;

    private final WaitingTicketRepository waitingTicketRepository;
    private final Clock clock;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void markNoShowExpired() {
        LocalDateTime threshold = LocalDateTime.now(clock).minusMinutes(NO_SHOW_TIMEOUT_MINUTES);
        List<WaitingTicket> expired = waitingTicketRepository
                .findByStatusAndCalledAtBefore(WaitingStatus.CALLED, threshold);
        expired.forEach(WaitingTicket::noShow);
    }
}
