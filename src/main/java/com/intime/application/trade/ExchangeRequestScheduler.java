package com.intime.application.trade;

import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.ExchangeRequestStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeRequestScheduler {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final Clock clock;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void expireRequests() {
        List<ExchangeRequest> expired = exchangeRequestRepository
                .findByStatusAndExpiresAtBefore(ExchangeRequestStatus.PENDING, LocalDateTime.now(clock));
        expired.forEach(ExchangeRequest::expire);
    }
}
