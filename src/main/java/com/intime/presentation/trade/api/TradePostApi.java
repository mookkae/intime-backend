package com.intime.presentation.trade.api;

import com.intime.presentation.trade.dto.TradePostCreateRequest;
import com.intime.presentation.trade.dto.TradePostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Trade Post", description = "순번 교환 게시글 관련 API")
public interface TradePostApi {

    @Operation(summary = "순번 교환 게시글 등록")
    @ApiResponse(responseCode = "201", description = "게시글 등록 성공")
    @PostMapping("/api/v1/waiting/{ticketId}/trade-post")
    ResponseEntity<TradePostResponse> register(
            @PathVariable Long ticketId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody TradePostCreateRequest request
    );

    @Operation(summary = "순번 교환 게시글 취소")
    @ApiResponse(responseCode = "204", description = "게시글 취소 성공")
    @DeleteMapping("/api/v1/trade-posts/{postId}")
    ResponseEntity<Void> withdraw(
            @PathVariable Long postId,
            @Parameter(description = "회원 ID") @RequestHeader("X-Member-Id") Long memberId
    );

    @Operation(summary = "가게 교환 게시글 목록 조회")
    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공")
    @GetMapping("/api/v1/stores/{storeId}/trade-posts")
    ResponseEntity<List<TradePostResponse>> getStoreTradePosts(@PathVariable Long storeId);
}
