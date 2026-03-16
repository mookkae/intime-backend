package com.intime.presentation.negotiation.dto;

import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationStatus;

import java.time.LocalDateTime;

public record NegotiationResponse(
        Long id,
        NegotiationStatus status,
        Long currentPrice,
        int offerCount,
        LocalDateTime expiresAt,
        Long lastOfferedBy
) {
    public static NegotiationResponse from(Negotiation negotiation) {
        return new NegotiationResponse(
                negotiation.getId(),
                negotiation.getStatus(),
                negotiation.getCurrentPrice(),
                negotiation.getOfferCount(),
                negotiation.getExpiresAt(),
                negotiation.getLastOfferedBy()
        );
    }
}
