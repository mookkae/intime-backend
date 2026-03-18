package com.intime.presentation.waiting;

import com.intime.application.waiting.WaitingQueryService;
import com.intime.application.waiting.dto.WaitingPositionInfo;
import com.intime.application.waiting.dto.WaitingTicketInfo;
import com.intime.presentation.waiting.api.WaitingQueryApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WaitingQueryController implements WaitingQueryApi {

    private final WaitingQueryService waitingQueryService;

    @GetMapping("/api/v1/stores/{storeId}/queue")
    public ResponseEntity<List<WaitingTicketInfo>> getStoreQueue(@PathVariable Long storeId) {
        return ResponseEntity.ok(waitingQueryService.getStoreQueue(storeId));
    }

    @GetMapping("/api/v1/waiting/me")
    public ResponseEntity<List<WaitingTicketInfo>> getMyTickets(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        return ResponseEntity.ok(waitingQueryService.getMyTickets(memberId));
    }

    @GetMapping("/api/v1/waiting/{ticketId}/position")
    public ResponseEntity<WaitingPositionInfo> getMyPosition(@PathVariable Long ticketId) {
        return ResponseEntity.ok(waitingQueryService.getMyPosition(ticketId));
    }
}
