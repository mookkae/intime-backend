package com.intime.application.store;

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
    public Store createStore(String name, String address, int estimatedWaitMinutes) {
        return storeRepository.save(Store.create(name, address, estimatedWaitMinutes));
    }

    @Override
    public Store getStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(StoreCode.STORE_NOT_FOUND));
    }

    @Override
    public List<Store> getStores() {
        return storeRepository.findAll();
    }
}
