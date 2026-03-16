package com.intime.domain.trade;

import com.intime.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TradePost 엔티티 단위 테스트")
class TradePostTest {

    private TradePost createDefaultPost() {
        return TradePost.create(1L, 1L, 1L, 5000L);
    }

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 생성 시 OPEN 상태")
        void createTradePost() {
            TradePost post = createDefaultPost();

            assertThat(post.getWaitingTicketId()).isEqualTo(1L);
            assertThat(post.getSellerId()).isEqualTo(1L);
            assertThat(post.getStoreId()).isEqualTo(1L);
            assertThat(post.getPrice()).isEqualTo(5000L);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.OPEN);
        }
    }

    @Nested
    @DisplayName("startNegotiation 메서드")
    class StartNegotiation {

        @Test
        @DisplayName("성공 : OPEN → NEGOTIATING")
        void startNegotiationFromOpen() {
            TradePost post = createDefaultPost();

            post.startNegotiation();

            assertThat(post.getStatus()).isEqualTo(TradePostStatus.NEGOTIATING);
        }

        @Test
        @DisplayName("실패 : NEGOTIATING → startNegotiation 예외")
        void startNegotiationFromNegotiating() {
            TradePost post = createDefaultPost();
            post.startNegotiation();

            assertThatThrownBy(post::startNegotiation)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : CLOSED → startNegotiation 예외")
        void startNegotiationFromClosed() {
            TradePost post = createDefaultPost();
            post.startNegotiation();
            post.close();

            assertThatThrownBy(post::startNegotiation)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("reopen 메서드")
    class Reopen {

        @Test
        @DisplayName("성공 : NEGOTIATING → OPEN")
        void reopenFromNegotiating() {
            TradePost post = createDefaultPost();
            post.startNegotiation();

            post.reopen();

            assertThat(post.getStatus()).isEqualTo(TradePostStatus.OPEN);
        }

        @Test
        @DisplayName("실패 : OPEN → reopen 예외")
        void reopenFromOpen() {
            TradePost post = createDefaultPost();

            assertThatThrownBy(post::reopen)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("close 메서드")
    class Close {

        @Test
        @DisplayName("성공 : NEGOTIATING → CLOSED")
        void closeFromNegotiating() {
            TradePost post = createDefaultPost();
            post.startNegotiation();

            post.close();

            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CLOSED);
        }

        @Test
        @DisplayName("실패 : OPEN → close 예외")
        void closeFromOpen() {
            TradePost post = createDefaultPost();

            assertThatThrownBy(post::close)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : OPEN → CANCELLED")
        void cancelFromOpen() {
            TradePost post = createDefaultPost();

            post.cancel();

            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : NEGOTIATING → CANCELLED")
        void cancelFromNegotiating() {
            TradePost post = createDefaultPost();
            post.startNegotiation();

            post.cancel();

            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : CLOSED → cancel 예외")
        void cancelFromClosed() {
            TradePost post = createDefaultPost();
            post.startNegotiation();
            post.close();

            assertThatThrownBy(post::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("isOwnedBy 메서드")
    class IsOwnedBy {

        @Test
        @DisplayName("본인 sellerId → true")
        void ownedBySeller() {
            TradePost post = createDefaultPost();

            assertThat(post.isOwnedBy(1L)).isTrue();
        }

        @Test
        @DisplayName("다른 sellerId → false")
        void notOwnedByOther() {
            TradePost post = createDefaultPost();

            assertThat(post.isOwnedBy(2L)).isFalse();
        }
    }
}
