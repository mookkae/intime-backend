package com.intime.application.waiting;

public record WaitingPositionResponse(
        Long ticketId,
        int positionNumber,
        int aheadCount,
        int estimatedWaitMinutes
) {
}
