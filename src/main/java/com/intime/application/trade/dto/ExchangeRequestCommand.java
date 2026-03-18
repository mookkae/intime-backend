package com.intime.application.trade.dto;

public record ExchangeRequestCommand(
        Long postId,
        Long buyerTicketId,
        Long buyerId,
        Long offerPrice
) {
}
