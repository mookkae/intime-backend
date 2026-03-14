package com.intime.presentation.trade.dto;

import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostStatus;

import java.time.LocalDateTime;

public record TradePostResponse(
        Long id,
        Long waitingTicketId,
        Long sellerId,
        Long storeId,
        TradePostStatus status,
        String description,
        LocalDateTime createdAt
) {
    public static TradePostResponse from(TradePost post) {
        return new TradePostResponse(
                post.getId(),
                post.getWaitingTicketId(),
                post.getSellerId(),
                post.getStoreId(),
                post.getStatus(),
                post.getDescription(),
                post.getCreatedAt()
        );
    }
}
