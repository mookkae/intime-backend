package com.intime.domain.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Store 엔티티 단위 테스트")
class StoreTest {

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 가게 생성")
        void createStore() {
            Store store = Store.create("하뚜분식", "서울시 양천구", 30);

            assertThat(store.getName()).isEqualTo("하뚜분식");
            assertThat(store.getAddress()).isEqualTo("서울시 양천구");
            assertThat(store.getEstimatedWaitMinutes()).isEqualTo(30);
        }
    }
}
