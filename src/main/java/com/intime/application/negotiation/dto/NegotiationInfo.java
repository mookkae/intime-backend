package com.intime.application.negotiation.dto;

import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationStatus;

import java.time.LocalDateTime;

public record NegotiationInfo(
        Long id,
        NegotiationStatus status,
        Long currentPrice,
        int offerCount,
        LocalDateTime expiresAt,
        Long lastOfferedBy,
        Long sellerId,
        Long buyerId
) {

    public static NegotiationInfo from(Negotiation negotiation) {
        return new NegotiationInfo(
                negotiation.getId(),
                negotiation.getStatus(),
                negotiation.getCurrentPrice(),
                negotiation.getOfferCount(),
                negotiation.getExpiresAt(),
                negotiation.getLastOfferedBy(),
                negotiation.getSellerId(),
                negotiation.getBuyerId()
        );
    }
}
