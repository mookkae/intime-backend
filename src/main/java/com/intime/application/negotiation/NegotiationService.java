package com.intime.application.negotiation;

import com.intime.application.negotiation.dto.NegotiationFinalOfferCommand;
import com.intime.application.negotiation.dto.NegotiationInfo;
import com.intime.application.negotiation.dto.NegotiationOfferCommand;

public interface NegotiationService {

    NegotiationInfo getNegotiation(Long negotiationId);

    NegotiationInfo getNegotiationByExchangeRequestId(Long exchangeRequestId);

    NegotiationInfo makeOffer(NegotiationOfferCommand command);

    void accept(Long negotiationId, Long memberId);

    void reject(Long negotiationId, Long memberId);

    void submitFinalOffer(NegotiationFinalOfferCommand command);
}
