package com.intime.presentation.negotiation;

import com.intime.application.negotiation.NegotiationService;
import com.intime.domain.negotiation.Negotiation;
import com.intime.presentation.negotiation.dto.NegotiationFinalOfferRequest;
import com.intime.presentation.negotiation.dto.NegotiationOfferRequest;
import com.intime.presentation.negotiation.dto.NegotiationResponse;
import com.intime.presentation.negotiation.api.NegotiationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NegotiationController implements NegotiationApi {

    private final NegotiationService negotiationService;

    @PostMapping("/api/v1/negotiations/{negotiationId}/offers")
    public ResponseEntity<NegotiationResponse> makeOffer(
            @PathVariable Long negotiationId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody NegotiationOfferRequest request
    ) {
        Negotiation negotiation = negotiationService.makeOffer(negotiationId, memberId, request.price());
        return ResponseEntity.ok(NegotiationResponse.from(negotiation));
    }

    @PostMapping("/api/v1/negotiations/{negotiationId}/accept")
    public ResponseEntity<Void> accept(
            @PathVariable Long negotiationId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        negotiationService.accept(negotiationId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/negotiations/{negotiationId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long negotiationId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        negotiationService.reject(negotiationId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/negotiations/{negotiationId}/final-offer")
    public ResponseEntity<Void> submitFinalOffer(
            @PathVariable Long negotiationId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody NegotiationFinalOfferRequest request
    ) {
        negotiationService.submitFinalOffer(negotiationId, memberId, request.price());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/negotiations/{negotiationId}")
    public ResponseEntity<NegotiationResponse> getNegotiation(@PathVariable Long negotiationId) {
        Negotiation negotiation = negotiationService.getNegotiation(negotiationId);
        return ResponseEntity.ok(NegotiationResponse.from(negotiation));
    }

    @GetMapping("/api/v1/exchange-requests/{requestId}/negotiation")
    public ResponseEntity<NegotiationResponse> getNegotiationByExchangeRequestId(@PathVariable Long requestId) {
        Negotiation negotiation = negotiationService.getNegotiationByExchangeRequestId(requestId);
        return ResponseEntity.ok(NegotiationResponse.from(negotiation));
    }
}
