package com.intime.application.store.dto;

import com.intime.domain.store.Store;

public record StoreInfo(
        Long id,
        String name,
        String address,
        int estimatedWaitMinutes
) {

    public static StoreInfo from(Store store) {
        return new StoreInfo(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getEstimatedWaitMinutes()
        );
    }
}
