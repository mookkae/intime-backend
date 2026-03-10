package com.intime.presentation.waiting;

import com.intime.application.waiting.WaitingPositionResponse;
import com.intime.application.waiting.WaitingQueryService;
import com.intime.common.ApiResponse;
import com.intime.domain.waiting.WaitingCode;
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
    public ResponseEntity<ApiResponse<List<WaitingTicketResponse>>> getStoreQueue(@PathVariable Long storeId) {
        List<WaitingTicketResponse> responses = waitingQueryService.getStoreQueue(storeId).stream()
                .map(WaitingTicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.of(WaitingCode.WAITING_FOUND, responses));
    }

    @GetMapping("/api/v1/waiting/me")
    public ResponseEntity<ApiResponse<List<WaitingTicketResponse>>> getMyTickets(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        List<WaitingTicketResponse> responses = waitingQueryService.getMyTickets(memberId).stream()
                .map(WaitingTicketResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.of(WaitingCode.WAITING_FOUND, responses));
    }

    @GetMapping("/api/v1/waiting/{ticketId}/position")
    public ResponseEntity<ApiResponse<WaitingPositionResponse>> getMyPosition(@PathVariable Long ticketId) {
        WaitingPositionResponse response = waitingQueryService.getMyPosition(ticketId);
        return ResponseEntity.ok(ApiResponse.of(WaitingCode.WAITING_FOUND, response));
    }
}
