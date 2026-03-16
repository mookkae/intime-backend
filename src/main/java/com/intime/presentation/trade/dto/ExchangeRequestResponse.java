package com.intime.presentation.trade.dto;

import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestStatus;

import java.time.LocalDateTime;

public record ExchangeRequestResponse(
        Long id,
        Long tradePostId,
        Long buyerTicketId,
        Long buyerId,
        Long offerPrice,
        ExchangeRequestStatus status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static ExchangeRequestResponse from(ExchangeRequest request) {
        return new ExchangeRequestResponse(
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
