package com.intime.presentation.waiting;

import com.intime.application.waiting.WaitingService;
import com.intime.application.waiting.dto.WaitingRegisterCommand;
import com.intime.application.waiting.dto.WaitingTicketInfo;
import com.intime.presentation.waiting.api.WaitingApi;
import com.intime.presentation.waiting.dto.WaitingRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingController implements WaitingApi {

    private final WaitingService waitingService;

    @PostMapping("/api/v1/stores/{storeId}/waiting")
    public ResponseEntity<WaitingTicketInfo> register(
            @PathVariable Long storeId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody WaitingRegisterRequest request
    ) {
        WaitingRegisterCommand command = new WaitingRegisterCommand(storeId, memberId, request.partySize());
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingService.register(command));
    }

    @DeleteMapping("/api/v1/waiting/{ticketId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long ticketId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        waitingService.cancel(ticketId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/stores/{storeId}/waiting/call-next")
    public ResponseEntity<WaitingTicketInfo> callNext(@PathVariable Long storeId) {
        return ResponseEntity.ok(waitingService.callNext(storeId));
    }

    @PatchMapping("/api/v1/waiting/{ticketId}/seated")
    public ResponseEntity<Void> confirmSeated(@PathVariable Long ticketId) {
        waitingService.confirmSeated(ticketId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/v1/waiting/{ticketId}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable Long ticketId) {
        waitingService.markNoShow(ticketId);
        return ResponseEntity.noContent().build();
    }
}
