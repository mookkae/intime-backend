package com.intime.support.fixture;

import com.intime.domain.store.Store;
import com.intime.support.TestReflectionUtils;

public class StoreFixture {

    public static Store createStore(Long storeId) {
        return createStore(storeId, "하뚜분식", "서울시 양천구", 30);
    }

    public static Store createStore(Long storeId, String name, String address, int estimatedWaitMinutes) {
        Store store = Store.create(name, address, estimatedWaitMinutes);
        if (storeId != null) {
            TestReflectionUtils.setField(store, "id", storeId);
        }
        return store;
    }
}
