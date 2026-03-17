package com.intime.presentation.waiting.api;

import com.intime.presentation.waiting.dto.WaitingRegisterRequest;
import com.intime.presentation.waiting.dto.WaitingTicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Waiting", description = "웨이팅 대기표 관련 API")
public interface WaitingApi {

    @Operation(summary = "웨이팅 등록")
    @ApiResponse(responseCode = "201", description = "웨이팅 등록 성공")
    @PostMapping("/api/v1/stores/{storeId}/waiting")
    ResponseEntity<WaitingTicketResponse> register(
            @PathVariable Long storeId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody WaitingRegisterRequest request
    );

    @Operation(summary = "웨이팅 취소")
    @ApiResponse(responseCode = "204", description = "웨이팅 취소 성공")
    @DeleteMapping("/api/v1/waiting/{ticketId}")
    ResponseEntity<Void> cancel(
            @PathVariable Long ticketId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "다음 순번 호출")
    @ApiResponse(responseCode = "200", description = "호출 성공")
    @PostMapping("/api/v1/stores/{storeId}/waiting/call-next")
    ResponseEntity<WaitingTicketResponse> callNext(@PathVariable Long storeId);

    @Operation(summary = "착석 확인")
    @ApiResponse(responseCode = "204", description = "착석 처리 성공")
    @PatchMapping("/api/v1/waiting/{ticketId}/seated")
    ResponseEntity<Void> confirmSeated(@PathVariable Long ticketId);

    @Operation(summary = "노쇼 처리")
    @ApiResponse(responseCode = "204", description = "노쇼 처리 성공")
    @PatchMapping("/api/v1/waiting/{ticketId}/no-show")
    ResponseEntity<Void> markNoShow(@PathVariable Long ticketId);
}
