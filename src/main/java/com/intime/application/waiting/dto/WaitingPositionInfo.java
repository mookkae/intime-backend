package com.intime.application.waiting.dto;

public record WaitingPositionInfo(
        Long ticketId,
        int positionNumber,
        int aheadCount,
        int estimatedWaitMinutes
) {
}
