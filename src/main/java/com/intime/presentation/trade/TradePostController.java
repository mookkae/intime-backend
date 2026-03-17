package com.intime.presentation.trade;

import com.intime.application.trade.TradePostService;
import com.intime.domain.trade.TradePost;
import com.intime.presentation.trade.dto.TradePostCreateRequest;
import com.intime.presentation.trade.dto.TradePostResponse;
import com.intime.presentation.trade.api.TradePostApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TradePostController implements TradePostApi {

    private final TradePostService tradePostService;

    @PostMapping("/api/v1/waiting/{ticketId}/trade-post")
    public ResponseEntity<TradePostResponse> register(
            @PathVariable Long ticketId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody TradePostCreateRequest request
    ) {
        TradePost post = tradePostService.register(ticketId, memberId, request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(TradePostResponse.from(post));
    }

    @DeleteMapping("/api/v1/trade-posts/{postId}")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long postId,
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        tradePostService.withdraw(postId, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/stores/{storeId}/trade-posts")
    public ResponseEntity<List<TradePostResponse>> getStoreTradePosts(@PathVariable Long storeId) {
        return ResponseEntity.ok(tradePostService.getStoreTradePosts(storeId).stream()
                .map(TradePostResponse::from)
                .toList());
    }
}
