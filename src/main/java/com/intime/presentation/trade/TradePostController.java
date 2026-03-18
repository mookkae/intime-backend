package com.intime.presentation.trade;

import com.intime.application.trade.TradePostService;
import com.intime.application.trade.dto.TradePostInfo;
import com.intime.application.trade.dto.TradePostRegisterCommand;
import com.intime.presentation.trade.api.TradePostApi;
import com.intime.presentation.trade.dto.TradePostCreateRequest;
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
    public ResponseEntity<TradePostInfo> register(
            @PathVariable Long ticketId,
            @RequestHeader("X-Member-Id") Long memberId,
            @RequestBody TradePostCreateRequest request
    ) {
        TradePostRegisterCommand command = new TradePostRegisterCommand(ticketId, memberId, request.price());
        return ResponseEntity.status(HttpStatus.CREATED).body(tradePostService.register(command));
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
    public ResponseEntity<List<TradePostInfo>> getStoreTradePosts(@PathVariable Long storeId) {
        return ResponseEntity.ok(tradePostService.getStoreTradePosts(storeId));
    }

    @GetMapping("/api/v1/trade-posts/my")
    public ResponseEntity<List<TradePostInfo>> getMyTradePosts(
            @RequestHeader("X-Member-Id") Long memberId
    ) {
        return ResponseEntity.ok(tradePostService.getMyTradePosts(memberId));
    }
}
