package com.intime.presentation.negotiation;

import com.intime.application.negotiation.NegotiationService;
import com.intime.application.negotiation.dto.NegotiationFinalOfferCommand;
import com.intime.application.negotiation.dto.NegotiationInfo;
import com.intime.application.negotiation.dto.NegotiationOfferCommand;
import com.intime.presentation.negotiation.api.NegotiationApi;
import com.intime.presentation.negotiation.dto.NegotiationFinalOfferRequest;
import com.intime.presentation.negotiation.dto.NegotiationOfferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NegotiationController implements NegotiationApi {

    private final NegotiationService negotiationService;

    @PostMapping("/api/v1/negotiations/{negotiationId}/offers")
    public ResponseEntity<NegotiationInfo> makeOffer(
            @PathVariable Long negotiationId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody NegotiationOfferRequest request
    ) {
        NegotiationOfferCommand command = new NegotiationOfferCommand(negotiationId, memberId, request.price());
        return ResponseEntity.ok(negotiationService.makeOffer(command));
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
        NegotiationFinalOfferCommand command = new NegotiationFinalOfferCommand(negotiationId, memberId, request.price());
        negotiationService.submitFinalOffer(command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/negotiations/{negotiationId}")
    public ResponseEntity<NegotiationInfo> getNegotiation(@PathVariable Long negotiationId) {
        return ResponseEntity.ok(negotiationService.getNegotiation(negotiationId));
    }

    @GetMapping("/api/v1/exchange-requests/{requestId}/negotiation")
    public ResponseEntity<NegotiationInfo> getNegotiationByExchangeRequestId(@PathVariable Long requestId) {
        return ResponseEntity.ok(negotiationService.getNegotiationByExchangeRequestId(requestId));
    }
}
