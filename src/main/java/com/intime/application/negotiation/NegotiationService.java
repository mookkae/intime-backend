package com.intime.application.negotiation;

import com.intime.domain.negotiation.Negotiation;

public interface NegotiationService {

    Negotiation getNegotiation(Long negotiationId);

    Negotiation makeOffer(Long negotiationId, Long memberId, Long price);

    void accept(Long negotiationId, Long memberId);

    void reject(Long negotiationId, Long memberId);

    void submitFinalOffer(Long negotiationId, Long memberId, Long price);
}
