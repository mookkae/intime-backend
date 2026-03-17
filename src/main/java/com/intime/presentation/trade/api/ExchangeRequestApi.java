package com.intime.presentation.trade.api;

import com.intime.presentation.trade.dto.ExchangeRequestCreateRequest;
import com.intime.presentation.trade.dto.ExchangeRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Exchange Request", description = "순번 교환 신청 관련 API")
public interface ExchangeRequestApi {

    @Operation(summary = "순번 교환 신청")
    @ApiResponse(responseCode = "201", description = "교환 신청 성공")
    @PostMapping("/api/v1/trade-posts/{postId}/requests")
    ResponseEntity<ExchangeRequestResponse> requestExchange(
            @PathVariable Long postId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody ExchangeRequestCreateRequest request
    );

    @Operation(summary = "교환 신청 취소")
    @ApiResponse(responseCode = "204", description = "교환 신청 취소 성공")
    @DeleteMapping("/api/v1/exchange-requests/{requestId}")
    ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "게시글 교환 신청 목록 조회")
    @ApiResponse(responseCode = "200", description = "신청 목록 조회 성공")
    @GetMapping("/api/v1/trade-posts/{postId}/requests")
    ResponseEntity<List<ExchangeRequestResponse>> getRequests(@PathVariable Long postId);

    @Operation(summary = "구매자 선택 (협상 자동 시작)")
    @ApiResponse(responseCode = "204", description = "구매자 선택 성공")
    @PostMapping("/api/v1/exchange-requests/{requestId}/select")
    ResponseEntity<Void> selectBuyer(
            @PathVariable Long requestId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );
}
