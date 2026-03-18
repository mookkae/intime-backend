package com.intime.presentation.trade;

import com.intime.application.trade.ExchangeRequestService;
import com.intime.application.trade.dto.ExchangeRequestCommand;
import com.intime.application.trade.dto.ExchangeRequestInfo;
import com.intime.presentation.trade.api.ExchangeRequestApi;
import com.intime.presentation.trade.dto.ExchangeRequestCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExchangeRequestController implements ExchangeRequestApi {

    private final ExchangeRequestService exchangeRequestService;

    @PostMapping("/api/v1/trade-posts/{postId}/requests")
    public ResponseEntity<ExchangeRequestInfo> requestExchange(
            @PathVariable Long postId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody ExchangeRequestCreateRequest request
    ) {
        ExchangeRequestCommand command = new ExchangeRequestCommand(
                postId, request.buyerTicketId(), memberId, request.offerPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(exchangeRequestService.requestExchange(command));
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
    public ResponseEntity<List<ExchangeRequestInfo>> getRequests(@PathVariable Long postId) {
        return ResponseEntity.ok(exchangeRequestService.getPostRequests(postId));
    }

    @PostMapping("/api/v1/exchange-requests/{requestId}/select")
    public ResponseEntity<Void> selectBuyer(
            @PathVariable Long requestId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        exchangeRequestService.selectBuyerAndStartNegotiation(requestId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/exchange-requests/my")
    public ResponseEntity<List<ExchangeRequestInfo>> getMyRequests(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        return ResponseEntity.ok(exchangeRequestService.getMyRequests(memberId));
    }
}
