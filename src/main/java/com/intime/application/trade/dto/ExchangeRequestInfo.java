package com.intime.application.trade.dto;

import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestStatus;

import java.time.LocalDateTime;

public record ExchangeRequestInfo(
        Long id,
        Long tradePostId,
        Long buyerTicketId,
        Long buyerId,
        Long offerPrice,
        ExchangeRequestStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {

    public static ExchangeRequestInfo from(ExchangeRequest request) {
        return new ExchangeRequestInfo(
                request.getId(),
                request.getTradePostId(),
                request.getBuyerTicketId(),
                request.getBuyerId(),
                request.getOfferPrice(),
                request.getStatus(),
                request.getExpiresAt(),
                request.getCreatedAt()
        );
    }
}
