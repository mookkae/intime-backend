package com.intime.presentation.waiting;

import com.intime.application.waiting.WaitingService;
import com.intime.common.ApiResponse;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.presentation.waiting.dto.WaitingRegisterRequest;
import com.intime.presentation.waiting.dto.WaitingTicketResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @PostMapping("/api/v1/stores/{storeId}/waiting")
    public ResponseEntity<ApiResponse<WaitingTicketResponse>> register(
            @PathVariable Long storeId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody WaitingRegisterRequest request
    ) {
        WaitingTicket ticket = waitingService.register(storeId, memberId, request.partySize());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(WaitingCode.WAITING_CREATED, WaitingTicketResponse.from(ticket)));
    }

    @DeleteMapping("/api/v1/waiting/{ticketId}")
    public ResponseEntity<ApiResponse<?>> cancel(
            @PathVariable Long ticketId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        waitingService.cancel(ticketId, memberId);
        return ResponseEntity.ok(ApiResponse.ok(WaitingCode.WAITING_CANCELLED));
    }

    @PostMapping("/api/v1/stores/{storeId}/waiting/call-next")
    public ResponseEntity<ApiResponse<WaitingTicketResponse>> callNext(@PathVariable Long storeId) {
        WaitingTicket ticket = waitingService.callNext(storeId);
        return ResponseEntity.ok(ApiResponse.of(WaitingCode.WAITING_CALLED, WaitingTicketResponse.from(ticket)));
    }

    @PatchMapping("/api/v1/waiting/{ticketId}/seated")
    public ResponseEntity<ApiResponse<?>> confirmSeated(@PathVariable Long ticketId) {
        waitingService.confirmSeated(ticketId);
        return ResponseEntity.ok(ApiResponse.ok(WaitingCode.WAITING_SEATED));
    }

    @PatchMapping("/api/v1/waiting/{ticketId}/no-show")
    public ResponseEntity<ApiResponse<?>> markNoShow(@PathVariable Long ticketId) {
        waitingService.markNoShow(ticketId);
        return ResponseEntity.ok(ApiResponse.ok(WaitingCode.WAITING_NO_SHOW));
    }
}
