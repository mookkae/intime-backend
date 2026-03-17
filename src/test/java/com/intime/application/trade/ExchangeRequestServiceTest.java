package com.intime.application.trade;

import com.intime.application.trade.fixture.ExchangeRequestFixture;
import com.intime.application.trade.fixture.TradePostFixture;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import com.intime.support.fixture.WaitingTicketFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationStatus;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRequestService 단위 테스트")
class ExchangeRequestServiceTest {

    @InjectMocks
    private ExchangeRequestServiceImpl exchangeRequestService;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private TradePostRepository tradePostRepository;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

    @Mock
    private NegotiationRepository negotiationRepository;

    @Mock
    private TradePostEventPublisher tradePostEventPublisher;

    @Mock
    private Clock clock;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 14, 12, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private void setupClock() {
        given(clock.instant()).willReturn(FIXED_CLOCK.instant());
        given(clock.getZone()).willReturn(FIXED_CLOCK.getZone());
    }

    @Nested
    @DisplayName("requestExchange 메서드")
    class RequestExchange {

        @Test
        @DisplayName("성공 : buyerTicket WAITING + 본인 소유로 신청, expiresAt = now + 5분")
        void requestExchangeSuccess() {
            // given
            setupClock();
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L); // sellerId=1
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(2L, 1L, 2L, 2, 2); // memberId=2
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));
            given(exchangeRequestRepository.existsByTradePostIdAndBuyerIdAndStatusIn(
                    1L, 2L, List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.SELECTED)))
                    .willReturn(false);
            given(waitingTicketRepository.findById(2L)).willReturn(Optional.of(buyerTicket));
            given(exchangeRequestRepository.save(any(ExchangeRequest.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            ExchangeRequest result = exchangeRequestService.requestExchange(1L, 2L, 2L, 10000L);

            // then
            assertThat(result.getStatus()).isEqualTo(ExchangeRequestStatus.PENDING);
            assertThat(result.getOfferPrice()).isEqualTo(10000L);
            assertThat(result.getExpiresAt()).isEqualTo(FIXED_NOW.plusMinutes(5));
        }

        @Test
        @DisplayName("실패 : 동일 포스트에 PENDING/SELECTED 신청이 있으면 예외")
        void requestExchangeDuplicate() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L); // sellerId=1
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));
            given(exchangeRequestRepository.existsByTradePostIdAndBuyerIdAndStatusIn(
                    1L, 2L, List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.SELECTED)))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.requestExchange(1L, 2L, 2L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_DUPLICATE);
        }

        @Test
        @DisplayName("실패 : buyerTicket WAITING 아닌 경우 예외")
        void buyerTicketNotWaiting() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(2L, 1L, 2L, 2, 2);
            buyerTicket.call(LocalDateTime.now());
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));
            given(exchangeRequestRepository.existsByTradePostIdAndBuyerIdAndStatusIn(
                    1L, 2L, List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.SELECTED)))
                    .willReturn(false);
            given(waitingTicketRepository.findById(2L)).willReturn(Optional.of(buyerTicket));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.requestExchange(1L, 2L, 2L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.BUYER_TICKET_NOT_WAITING);
        }

        @Test
        @DisplayName("실패 : 본인 포스트에 신청 시 예외")
        void requestSelfPost() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 10L, 2L, 1L); // sellerId=2 (same as buyerId)
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.requestExchange(1L, 2L, 2L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_SELF_POST);
        }

        @Test
        @DisplayName("실패 : buyerTicket 소유자 아닌 경우 예외")
        void buyerTicketNotOwner() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(2L, 1L, 3L, 2, 2); // owned by memberId=3
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));
            given(exchangeRequestRepository.existsByTradePostIdAndBuyerIdAndStatusIn(
                    1L, 2L, List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.SELECTED)))
                    .willReturn(false);
            given(waitingTicketRepository.findById(2L)).willReturn(Optional.of(buyerTicket));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.requestExchange(1L, 2L, 2L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.BUYER_TICKET_NOT_OWNER);
        }
    }

    @Nested
    @DisplayName("cancelRequest 메서드")
    class CancelRequest {

        @Test
        @DisplayName("성공 : 본인 신청 취소")
        void cancelSuccess() {
            // given
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));

            // when
            exchangeRequestService.cancelRequest(1L, 2L);

            // then
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : 타인 신청 취소 시 예외")
        void cancelNotOwner() {
            // given
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.cancelRequest(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_OWNER);
        }
    }

    @Nested
    @DisplayName("selectBuyerAndStartNegotiation 메서드")
    class SelectBuyer {

        @Test
        @DisplayName("성공 : 구매자 선택 → request SELECTED, TradePost NEGOTIATING, Negotiation 자동 생성")
        void selectBuyerAndStartNegotiationSuccess() {
            // given
            setupClock();
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L); // sellerId=1
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));
            given(negotiationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // when
            exchangeRequestService.selectBuyerAndStartNegotiation(1L, 1L);

            // then
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.SELECTED);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.NEGOTIATING);

            ArgumentCaptor<Negotiation> captor = ArgumentCaptor.forClass(Negotiation.class);
            verify(negotiationRepository).save(captor.capture());
            Negotiation savedNegotiation = captor.getValue();
            assertThat(savedNegotiation.getStatus()).isEqualTo(NegotiationStatus.NEGOTIATING);
            assertThat(savedNegotiation.getSellerId()).isEqualTo(1L);
            assertThat(savedNegotiation.getBuyerId()).isEqualTo(2L);
            assertThat(savedNegotiation.getCurrentPrice()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("실패 : 타인이 선택 시 예외")
        void selectByNonOwner() {
            // given
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L); // sellerId=1
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.selectBuyerAndStartNegotiation(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_NOT_OWNER);
        }

        @Test
        @DisplayName("실패 : OPEN 아닌 TradePost에서 선택 시 예외")
        void selectOnClosedPost() {
            // given
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            post.startNegotiation();
            post.close(); // NEGOTIATING → CLOSED
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> exchangeRequestService.selectBuyerAndStartNegotiation(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }
}
