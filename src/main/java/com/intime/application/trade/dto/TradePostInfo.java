package com.intime.application.trade.dto;

import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostStatus;

import java.time.LocalDateTime;

public record TradePostInfo(
        Long id,
        Long waitingTicketId,
        Long sellerId,
        Long storeId,
        TradePostStatus status,
        Long price,
        LocalDateTime createdAt
) {

    public static TradePostInfo from(TradePost post) {
        return new TradePostInfo(
                post.getId(),
                post.getWaitingTicketId(),
                post.getSellerId(),
                post.getStoreId(),
                post.getStatus(),
                post.getPrice(),
                post.getCreatedAt()
        );
    }
}
