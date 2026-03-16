package com.intime.domain.trade;

import com.intime.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ExchangeRequest 엔티티 단위 테스트")
class ExchangeRequestTest {

    private static final LocalDateTime EXPIRES_AT = LocalDateTime.of(2026, 3, 15, 12, 5, 0);

    private ExchangeRequest createDefaultRequest() {
        return ExchangeRequest.create(1L, 2L, 3L, 10000L, EXPIRES_AT);
    }

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 생성 시 PENDING 상태")
        void createExchangeRequest() {
            ExchangeRequest request = createDefaultRequest();

            assertThat(request.getTradePostId()).isEqualTo(1L);
            assertThat(request.getBuyerTicketId()).isEqualTo(2L);
            assertThat(request.getBuyerId()).isEqualTo(3L);
            assertThat(request.getOfferPrice()).isEqualTo(10000L);
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.PENDING);
            assertThat(request.getExpiresAt()).isEqualTo(EXPIRES_AT);
        }
    }

    @Nested
    @DisplayName("select 메서드")
    class Select {

        @Test
        @DisplayName("성공 : PENDING → SELECTED")
        void selectFromPending() {
            ExchangeRequest request = createDefaultRequest();

            request.select();

            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.SELECTED);
        }

        @Test
        @DisplayName("실패 : SELECTED → select 예외")
        void selectFromSelected() {
            ExchangeRequest request = createDefaultRequest();
            request.select();

            assertThatThrownBy(request::select)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : EXPIRED → select 예외")
        void selectFromExpired() {
            ExchangeRequest request = createDefaultRequest();
            request.expire();

            assertThatThrownBy(request::select)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("complete 메서드")
    class Complete {

        @Test
        @DisplayName("성공 : SELECTED → COMPLETED")
        void completeFromSelected() {
            ExchangeRequest request = createDefaultRequest();
            request.select();

            request.complete();

            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.COMPLETED);
        }

        @Test
        @DisplayName("실패 : PENDING → complete 예외")
        void completeFromPending() {
            ExchangeRequest request = createDefaultRequest();

            assertThatThrownBy(request::complete)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : PENDING → CANCELLED")
        void cancelFromPending() {
            ExchangeRequest request = createDefaultRequest();

            request.cancel();

            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : SELECTED → CANCELLED")
        void cancelFromSelected() {
            ExchangeRequest request = createDefaultRequest();
            request.select();

            request.cancel();

            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : COMPLETED → cancel 예외")
        void cancelFromCompleted() {
            ExchangeRequest request = createDefaultRequest();
            request.select();
            request.complete();

            assertThatThrownBy(request::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : EXPIRED → cancel 예외")
        void cancelFromExpired() {
            ExchangeRequest request = createDefaultRequest();
            request.expire();

            assertThatThrownBy(request::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("expire 메서드")
    class Expire {

        @Test
        @DisplayName("성공 : PENDING → EXPIRED")
        void expireFromPending() {
            ExchangeRequest request = createDefaultRequest();

            request.expire();

            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.EXPIRED);
        }

        @Test
        @DisplayName("실패 : SELECTED → expire 예외")
        void expireFromSelected() {
            ExchangeRequest request = createDefaultRequest();
            request.select();

            assertThatThrownBy(request::expire)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("isOwnedBy 메서드")
    class IsOwnedBy {

        @Test
        @DisplayName("본인 buyerId → true")
        void ownedByBuyer() {
            ExchangeRequest request = createDefaultRequest();

            assertThat(request.isOwnedBy(3L)).isTrue();
        }

        @Test
        @DisplayName("다른 buyerId → false")
        void notOwnedByOther() {
            ExchangeRequest request = createDefaultRequest();

            assertThat(request.isOwnedBy(99L)).isFalse();
        }
    }
}
