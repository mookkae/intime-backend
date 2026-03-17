package com.intime.application.negotiation.fixture;

import com.intime.domain.negotiation.Negotiation;
import com.intime.support.TestReflectionUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NegotiationFixture {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2026, 3, 15, 12, 0, 0)
                    .atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    // exchangeRequestId=1, sellerId=1, buyerId=2, sellerTicketId=10, buyerTicketId=20, price=10000
    public static Negotiation createNegotiation() {
        return createNegotiation(1L, 1L, 1L, 2L, 10L, 20L, 10000L);
    }

    public static Negotiation createNegotiation(Long negotiationId, Long exchangeRequestId,
                                                Long sellerId, Long buyerId,
                                                Long sellerTicketId, Long buyerTicketId,
                                                Long initialPrice) {
        Negotiation negotiation = Negotiation.create(
                exchangeRequestId, sellerId, buyerId,
                sellerTicketId, buyerTicketId,
                initialPrice, FIXED_CLOCK
        );
        if (negotiationId != null) {
            TestReflectionUtils.setField(negotiation, "id", negotiationId);
        }
        return negotiation;
    }

    /**
     * 기본 협상에서 6회 오퍼를 진행해 FINAL_ROUND 상태로 만든다.
     * sellerId=1(lastOfferedBy), buyerId=2 순서로 오퍼.
     * FINAL_ROUND 진입 시 lastOfferedBy=seller(1L).
     */
    public static Negotiation createFinalRoundNegotiation() {
        Negotiation negotiation = createNegotiation(1L, 1L, 1L, 2L, 10L, 20L, 10000L);
        negotiation.makeOffer(1L, 9000L); // 2
        negotiation.makeOffer(2L, 8500L); // 3
        negotiation.makeOffer(1L, 8000L); // 4
        negotiation.makeOffer(2L, 7500L); // 5
        negotiation.makeOffer(1L, 7000L); // 6 → FINAL_ROUND
        return negotiation;
    }
}
