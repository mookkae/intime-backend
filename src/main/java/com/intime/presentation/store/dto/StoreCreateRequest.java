package com.intime.presentation.store.dto;

public record StoreCreateRequest(
        String name,
        String address,
        int estimatedWaitMinutes
) {
}
