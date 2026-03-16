package com.intime.application.negotiation;

import java.time.LocalDateTime;

public record NegotiationEventDto(
        EventType type,
        Long currentPrice,
        int offerCount,
        LocalDateTime expiresAt
) {
    public enum EventType {
        OFFER, FINAL_ROUND, FINAL_OFFER_SUBMITTED, ACCEPTED, REJECTED, EXPIRED, CANCELLED
    }

    public static NegotiationEventDto ofOffer(Long price, int offerCount, LocalDateTime expiresAt) {
        return new NegotiationEventDto(EventType.OFFER, price, offerCount, expiresAt);
    }

    public static NegotiationEventDto ofFinalRound(LocalDateTime expiresAt) {
        return new NegotiationEventDto(EventType.FINAL_ROUND, null, 0, expiresAt);
    }

    public static NegotiationEventDto ofFinalOfferSubmitted() {
        return new NegotiationEventDto(EventType.FINAL_OFFER_SUBMITTED, null, 0, null);
    }

    public static NegotiationEventDto ofAccepted(Long price) {
        return new NegotiationEventDto(EventType.ACCEPTED, price, 0, null);
    }

    public static NegotiationEventDto ofRejected() {
        return new NegotiationEventDto(EventType.REJECTED, null, 0, null);
    }

    public static NegotiationEventDto ofExpired() {
        return new NegotiationEventDto(EventType.EXPIRED, null, 0, null);
    }
}
