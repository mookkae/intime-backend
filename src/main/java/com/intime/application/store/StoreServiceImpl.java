package com.intime.application.store;

import com.intime.application.store.dto.StoreCreateCommand;
import com.intime.application.store.dto.StoreInfo;
import com.intime.common.exception.BusinessException;
import com.intime.domain.store.Store;
import com.intime.domain.store.StoreCode;
import com.intime.domain.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    @Override
    @Transactional
    public StoreInfo createStore(StoreCreateCommand command) {
        Store store = storeRepository.save(
                Store.create(command.name(), command.address(), command.estimatedWaitMinutes()));
        return StoreInfo.from(store);
    }

    @Override
    public StoreInfo getStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(StoreCode.STORE_NOT_FOUND));
        return StoreInfo.from(store);
    }

    @Override
    public List<StoreInfo> getStores() {
        return storeRepository.findAll().stream()
                .map(StoreInfo::from)
                .toList();
    }
}
