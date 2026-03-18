package com.intime.application.negotiation.dto;

public record NegotiationFinalOfferCommand(
        Long negotiationId,
        Long memberId,
        Long price
) {
}
