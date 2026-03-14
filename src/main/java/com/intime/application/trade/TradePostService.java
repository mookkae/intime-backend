package com.intime.application.trade;

import com.intime.domain.trade.TradePost;

import java.util.List;

public interface TradePostService {
    TradePost register(Long ticketId, Long sellerId, String description);

    void withdraw(Long postId, Long sellerId);

    List<TradePost> getStoreTradePosts(Long storeId);
}
