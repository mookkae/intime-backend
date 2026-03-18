package com.intime.application.store;

import com.intime.application.store.dto.StoreCreateCommand;
import com.intime.application.store.dto.StoreInfo;

import java.util.List;

public interface StoreService {

    StoreInfo createStore(StoreCreateCommand command);

    StoreInfo getStore(Long storeId);

    List<StoreInfo> getStores();
}
