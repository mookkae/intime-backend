package com.intime.application.waiting.dto;

public record WaitingRegisterCommand(
        Long storeId,
        Long memberId,
        int partySize
) {
}
