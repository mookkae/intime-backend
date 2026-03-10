package com.intime.application.store;

import com.intime.domain.store.Store;

import java.util.List;

public interface StoreService {

    Store createStore(String name, String address, int estimatedWaitMinutes);

    Store getStore(Long storeId);

    List<Store> getStores();
}
