package com.intime.application.negotiation.dto;

public record NegotiationOfferCommand(
        Long negotiationId,
        Long memberId,
        Long price
) {
}
