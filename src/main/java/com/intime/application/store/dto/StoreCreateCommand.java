package com.intime.application.store.dto;

public record StoreCreateCommand(
        String name,
        String address,
        int estimatedWaitMinutes
) {
}
