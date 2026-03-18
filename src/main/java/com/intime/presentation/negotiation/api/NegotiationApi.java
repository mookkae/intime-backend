package com.intime.presentation.negotiation.api;

import com.intime.application.negotiation.dto.NegotiationInfo;
import com.intime.presentation.negotiation.dto.NegotiationFinalOfferRequest;
import com.intime.presentation.negotiation.dto.NegotiationOfferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Negotiation", description = "가격 협상 관련 API")
public interface NegotiationApi {

    @Operation(summary = "오퍼 제시")
    @ApiResponse(responseCode = "200", description = "오퍼 제시 성공")
    @PostMapping("/api/v1/negotiations/{negotiationId}/offers")
    ResponseEntity<NegotiationInfo> makeOffer(
            @PathVariable Long negotiationId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody NegotiationOfferRequest request
    );

    @Operation(summary = "오퍼 수락")
    @ApiResponse(responseCode = "204", description = "오퍼 수락 성공")
    @PostMapping("/api/v1/negotiations/{negotiationId}/accept")
    ResponseEntity<Void> accept(
            @PathVariable Long negotiationId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "오퍼 거절")
    @ApiResponse(responseCode = "204", description = "오퍼 거절 성공")
    @PostMapping("/api/v1/negotiations/{negotiationId}/reject")
    ResponseEntity<Void> reject(
            @PathVariable Long negotiationId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "최종 입찰 제출 (FINAL_ROUND)")
    @ApiResponse(responseCode = "204", description = "최종 입찰 제출 성공")
    @PostMapping("/api/v1/negotiations/{negotiationId}/final-offer")
    ResponseEntity<Void> submitFinalOffer(
            @PathVariable Long negotiationId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody NegotiationFinalOfferRequest request
    );

    @Operation(summary = "협상 조회")
    @ApiResponse(responseCode = "200", description = "협상 조회 성공")
    @GetMapping("/api/v1/negotiations/{negotiationId}")
    ResponseEntity<NegotiationInfo> getNegotiation(@PathVariable Long negotiationId);

    @Operation(summary = "교환 신청으로 협상 조회")
    @ApiResponse(responseCode = "200", description = "협상 조회 성공")
    @GetMapping("/api/v1/exchange-requests/{requestId}/negotiation")
    ResponseEntity<NegotiationInfo> getNegotiationByExchangeRequestId(@PathVariable Long requestId);
}
