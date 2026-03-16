package com.intime.application.negotiation;

import com.intime.application.trade.TradeLifecycleService;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NegotiationScheduler {

    private final NegotiationRepository negotiationRepository;
    private final TradeLifecycleService tradeLifecycleService;
    private final NegotiationEventPublisher eventPublisher;
    private final Clock clock;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void expireNegotiations() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Negotiation> expired = negotiationRepository.findByStatusInAndExpiresAtBefore(
                NegotiationStatus.ACTIVE_STATUSES, now);

        for (Negotiation negotiation : expired) {
            try {
                negotiation.expire();
                tradeLifecycleService.cancelRequestAndReopenPost(negotiation.getExchangeRequestId());
                eventPublisher.publish(negotiation.getId(), NegotiationEventDto.ofExpired());
            } catch (Exception e) {
                log.warn("협상 만료 처리 실패 - negotiationId: {}", negotiation.getId(), e);
            }
        }
    }
}
