package com.intime.presentation.waiting;

import com.intime.application.waiting.WaitingPositionResponse;
import com.intime.application.waiting.WaitingQueryService;
import com.intime.presentation.waiting.dto.WaitingTicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WaitingQueryController {

    private final WaitingQueryService waitingQueryService;

    @GetMapping("/api/v1/stores/{storeId}/queue")
    public ResponseEntity<List<WaitingTicketResponse>> getStoreQueue(@PathVariable Long storeId) {
        List<WaitingTicketResponse> responses = waitingQueryService.getStoreQueue(storeId).stream()
                .map(WaitingTicketResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/waiting/me")
    public ResponseEntity<List<WaitingTicketResponse>> getMyTickets(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        List<WaitingTicketResponse> responses = waitingQueryService.getMyTickets(memberId).stream()
                .map(WaitingTicketResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/v1/waiting/{ticketId}/position")
    public ResponseEntity<WaitingPositionResponse> getMyPosition(@PathVariable Long ticketId) {
        WaitingPositionResponse response = waitingQueryService.getMyPosition(ticketId);
        return ResponseEntity.ok(response);
    }
}
