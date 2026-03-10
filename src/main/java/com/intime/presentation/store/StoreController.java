package com.intime.presentation.store;

import com.intime.application.store.StoreService;
import com.intime.common.ApiResponse;
import com.intime.domain.store.Store;
import com.intime.domain.store.StoreCode;
import com.intime.presentation.store.dto.StoreCreateRequest;
import com.intime.presentation.store.dto.StoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreResponse>> createStore(@RequestBody StoreCreateRequest request) {
        Store store = storeService.createStore(request.name(), request.address(), request.estimatedWaitMinutes());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(StoreCode.STORE_CREATED, StoreResponse.from(store)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getStores() {
        List<StoreResponse> responses = storeService.getStores().stream()
                .map(StoreResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.of(StoreCode.STORE_FOUND, responses));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreResponse>> getStore(@PathVariable Long storeId) {
        Store store = storeService.getStore(storeId);
        return ResponseEntity.ok(ApiResponse.of(StoreCode.STORE_FOUND, StoreResponse.from(store)));
    }
}
