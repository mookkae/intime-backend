package com.intime.presentation.store;

import com.intime.application.store.StoreService;
import com.intime.application.store.dto.StoreCreateCommand;
import com.intime.application.store.dto.StoreInfo;
import com.intime.presentation.store.api.StoreApi;
import com.intime.presentation.store.dto.StoreCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stores")
public class StoreController implements StoreApi {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<StoreInfo> createStore(@RequestBody StoreCreateRequest request) {
        StoreCreateCommand command = new StoreCreateCommand(
                request.name(), request.address(), request.estimatedWaitMinutes());
        return ResponseEntity.status(HttpStatus.CREATED).body(storeService.createStore(command));
    }

    @GetMapping
    public ResponseEntity<List<StoreInfo>> getStores() {
        return ResponseEntity.ok(storeService.getStores());
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<StoreInfo> getStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStore(storeId));
    }
}
