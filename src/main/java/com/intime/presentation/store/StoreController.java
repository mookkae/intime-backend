package com.intime.presentation.store;

import com.intime.application.store.StoreService;
import com.intime.domain.store.Store;
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
    public ResponseEntity<StoreResponse> createStore(@RequestBody StoreCreateRequest request) {
        Store store = storeService.createStore(request.name(), request.address(), request.estimatedWaitMinutes());
        return ResponseEntity.status(HttpStatus.CREATED).body(StoreResponse.from(store));
    }

    @GetMapping
    public ResponseEntity<List<StoreResponse>> getStores() {
        return ResponseEntity.ok(storeService.getStores().stream()
                .map(StoreResponse::from)
                .toList());
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreResponse> getStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(StoreResponse.from(storeService.getStore(storeId)));
    }
}
