package com.intime.application.trade;

import com.intime.application.negotiation.fixture.NegotiationFixture;
import com.intime.application.trade.fixture.ExchangeRequestFixture;
import com.intime.application.trade.fixture.TradePostFixture;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.ExchangeRequestStatus;
import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostRepository;
import com.intime.domain.trade.TradePostStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradeLifecycleService 단위 테스트")
class TradeLifecycleServiceTest {

    @InjectMocks
    private TradeLifecycleServiceImpl tradeLifecycleService;

    @Mock
    private NegotiationRepository negotiationRepository;
    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;
    @Mock
    private TradePostRepository tradePostRepository;

    @Nested
    @DisplayName("cancelActiveNegotiationByTicket 메서드")
    class CancelActiveNegotiationByTicket {

        @Test
        @DisplayName("성공 : 활성 협상 존재 → Negotiation/ExchangeRequest/TradePost 전부 CANCELLED")
        void cancelsAll() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(1L, 1L, 1L, 2L, 10L, 20L, 10000L);
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 20L, 2L);
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            post.startNegotiation();

            given(negotiationRepository.findBySellerTicketIdAndStatusIn(10L, NegotiationStatus.ACTIVE_STATUSES))
                    .willReturn(Optional.of(negotiation));
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            tradeLifecycleService.cancelActiveNegotiationByTicket(10L);

            // then
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.CANCELLED);
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.CANCELLED);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : 활성 협상 없음 → 아무것도 변경되지 않음")
        void noActiveNegotiationNoChange() {
            // given
            given(negotiationRepository.findBySellerTicketIdAndStatusIn(10L, NegotiationStatus.ACTIVE_STATUSES))
                    .willReturn(Optional.empty());

            // when
            tradeLifecycleService.cancelActiveNegotiationByTicket(10L);

            // then - exchangeRequestRepository, tradePostRepository 조회 없음
        }
    }

    @Nested
    @DisplayName("cancelRequestAndReopenPost 메서드")
    class CancelRequestAndReopenPost {

        @Test
        @DisplayName("성공 : ExchangeRequest CANCELLED, TradePost OPEN 복귀")
        void cancelsRequestAndReopensPost() {
            // given
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 20L, 2L);
            request.select();
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            post.startNegotiation();

            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            tradeLifecycleService.cancelRequestAndReopenPost(1L);

            // then
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.CANCELLED);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.OPEN);
        }
    }
}
