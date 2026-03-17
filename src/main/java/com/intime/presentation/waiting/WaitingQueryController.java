package com.intime.presentation.waiting;

import com.intime.application.trade.TradePostService;
import com.intime.application.waiting.WaitingPositionResponse;
import com.intime.application.waiting.WaitingQueryService;
import com.intime.domain.trade.TradePost;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.presentation.waiting.dto.WaitingTicketResponse;
import com.intime.presentation.waiting.dto.WaitingTicketResponse.TradePostInfo;
import com.intime.presentation.waiting.api.WaitingQueryApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class WaitingQueryController implements WaitingQueryApi {

    private final WaitingQueryService waitingQueryService;
    private final TradePostService tradePostService;

    @GetMapping("/api/v1/stores/{storeId}/queue")
    public ResponseEntity<List<WaitingTicketResponse>> getStoreQueue(@PathVariable Long storeId) {
        List<WaitingTicket> tickets = waitingQueryService.getStoreQueue(storeId);

        Map<Long, TradePostInfo> tradePostByTicketId = tradePostService.getStoreTradePosts(storeId).stream()
                .collect(Collectors.toMap(
                        TradePost::getWaitingTicketId,
                        tp -> new TradePostInfo(tp.getId(), tp.getPrice())
                ));

        return ResponseEntity.ok(tickets.stream()
                .map(ticket -> WaitingTicketResponse.from(ticket, tradePostByTicketId.get(ticket.getId())))
                .toList());
    }

    @GetMapping("/api/v1/waiting/me")
    public ResponseEntity<List<WaitingTicketResponse>> getMyTickets(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        return ResponseEntity.ok(waitingQueryService.getMyTickets(memberId).stream()
                .map(WaitingTicketResponse::from)
                .toList());
    }

    @GetMapping("/api/v1/waiting/{ticketId}/position")
    public ResponseEntity<WaitingPositionResponse> getMyPosition(@PathVariable Long ticketId) {
        return ResponseEntity.ok(waitingQueryService.getMyPosition(ticketId));
    }
}
