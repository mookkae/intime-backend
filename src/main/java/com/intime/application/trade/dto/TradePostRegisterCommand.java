package com.intime.application.trade.dto;

public record TradePostRegisterCommand(
        Long ticketId,
        Long sellerId,
        Long price
) {
}
