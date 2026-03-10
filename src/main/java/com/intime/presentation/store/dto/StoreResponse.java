package com.intime.presentation.store.dto;

import com.intime.domain.store.Store;

public record StoreResponse(
        Long id,
        String name,
        String address,
        int estimatedWaitMinutes
) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getEstimatedWaitMinutes()
        );
    }
}
