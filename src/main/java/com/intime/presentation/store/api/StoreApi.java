package com.intime.presentation.store.api;

import com.intime.application.store.dto.StoreInfo;
import com.intime.presentation.store.dto.StoreCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Store", description = "가게 관련 API")
@RequestMapping("/api/v1/stores")
public interface StoreApi {

    @Operation(summary = "가게 등록")
    @ApiResponse(responseCode = "201", description = "가게 등록 성공")
    @PostMapping
    ResponseEntity<StoreInfo> createStore(@RequestBody StoreCreateRequest request);

    @Operation(summary = "가게 목록 조회")
    @ApiResponse(responseCode = "200", description = "가게 목록 조회 성공")
    @GetMapping
    ResponseEntity<List<StoreInfo>> getStores();

    @Operation(summary = "가게 단건 조회")
    @ApiResponse(responseCode = "200", description = "가게 단건 조회 성공")
    @GetMapping("/{storeId}")
    ResponseEntity<StoreInfo> getStore(@PathVariable Long storeId);
}
