package com.intime.application.store;

import com.intime.application.store.dto.StoreCreateCommand;
import com.intime.application.store.dto.StoreInfo;
import com.intime.common.exception.BusinessException;
import com.intime.domain.store.Store;
import com.intime.domain.store.StoreCode;
import com.intime.domain.store.StoreRepository;
import com.intime.support.fixture.StoreFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 단위 테스트")
class StoreServiceTest {

    @InjectMocks
    private StoreServiceImpl storeService;

    @Mock
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("createStore 메서드")
    class CreateStore {

        @Test
        @DisplayName("성공 : 가게 생성")
        void createStore() {
            // given
            StoreCreateCommand command = new StoreCreateCommand("하뚜분식", "서울시 양천구", 30);
            given(storeRepository.save(any(Store.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            StoreInfo result = storeService.createStore(command);

            // then
            assertThat(result.name()).isEqualTo("하뚜분식");
            assertThat(result.address()).isEqualTo("서울시 양천구");
            assertThat(result.estimatedWaitMinutes()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("getStore 메서드")
    class GetStore {

        @Test
        @DisplayName("성공 : 가게 조회")
        void getStore() {
            // given
            Store store = StoreFixture.createStore(1L);
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));

            // when
            StoreInfo result = storeService.getStore(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 : 존재하지 않는 가게")
        void storeNotFound() {
            // given
            given(storeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> storeService.getStore(1L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getBaseCode())
                            .isEqualTo(StoreCode.STORE_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("getStores 메서드")
    class GetStores {

        @Test
        @DisplayName("성공 : 가게 목록 조회")
        void getStores() {
            // given
            List<Store> stores = List.of(
                    StoreFixture.createStore(1L),
                    StoreFixture.createStore(2L, "또다른 식당", "서울시 서초구", 20)
            );
            given(storeRepository.findAll()).willReturn(stores);

            // when
            List<StoreInfo> result = storeService.getStores();

            // then
            assertThat(result).hasSize(2);
        }
    }
}
