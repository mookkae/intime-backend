package com.intime.presentation.trade;

import com.intime.application.trade.ExchangeRequestService;
import com.intime.domain.trade.ExchangeRequest;
import com.intime.presentation.trade.dto.ExchangeRequestCreateRequest;
import com.intime.presentation.trade.dto.ExchangeRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeRequestController {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping("/api/v1/trade-posts/{postId}/requests")
    public ResponseEntity<ExchangeRequestResponse> requestExchange(
            @PathVariable Long postId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody ExchangeRequestCreateRequest request
    ) {
        ExchangeRequest exchangeRequest = exchangeRequestService.requestExchange(postId, request.buyerTicketId(), memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExchangeRequestResponse.from(exchangeRequest));
    }

    @DeleteMapping("/api/v1/exchange-requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        exchangeRequestService.cancelRequest(requestId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/trade-posts/{postId}/requests")
    public ResponseEntity<List<ExchangeRequestResponse>> getRequests(@PathVariable Long postId) {
        return ResponseEntity.ok(exchangeRequestService.getPostRequests(postId).stream()
                .map(ExchangeRequestResponse::from)
                .toList());
    }

    @PostMapping("/api/v1/exchange-requests/{requestId}/select")
    public ResponseEntity<Void> selectBuyer(
            @PathVariable Long requestId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        exchangeRequestService.selectBuyer(requestId, memberId);
        return ResponseEntity.noContent().build();
    }
}
