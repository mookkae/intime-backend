package com.intime.presentation.waiting.api;

import com.intime.application.waiting.WaitingPositionResponse;
import com.intime.presentation.waiting.dto.WaitingTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Waiting Query", description = "웨이팅 조회 관련 API")
public interface WaitingQueryApi {

    @Operation(summary = "가게 대기열 조회")
    @ApiResponse(responseCode = "200", description = "대기열 조회 성공")
    @GetMapping("/api/v1/stores/{storeId}/queue")
    ResponseEntity<List<WaitingTicketResponse>> getStoreQueue(@PathVariable Long storeId);

    @Operation(summary = "내 대기표 목록 조회")
    @ApiResponse(responseCode = "200", description = "내 대기표 조회 성공")
    @GetMapping("/api/v1/waiting/me")
    ResponseEntity<List<WaitingTicketResponse>> getMyTickets(
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "대기 순번 조회")
    @ApiResponse(responseCode = "200", description = "순번 조회 성공")
    @GetMapping("/api/v1/waiting/{ticketId}/position")
    ResponseEntity<WaitingPositionResponse> getMyPosition(@PathVariable Long ticketId);
}
