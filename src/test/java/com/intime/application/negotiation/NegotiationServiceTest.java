package com.intime.application.negotiation;

import com.intime.application.negotiation.fixture.NegotiationFixture;
import com.intime.application.trade.TradeLifecycleService;
import com.intime.application.trade.fixture.ExchangeRequestFixture;
import com.intime.application.trade.fixture.TradePostFixture;
import com.intime.application.waiting.WaitingEventPublisher;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.*;
import com.intime.domain.trade.*;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NegotiationService 단위 테스트")
class NegotiationServiceTest {

    @InjectMocks
    private NegotiationServiceImpl negotiationService;

    @Mock
    private NegotiationRepository negotiationRepository;
    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;
    @Mock
    private TradePostRepository tradePostRepository;
    @Mock
    private WaitingTicketRepository waitingTicketRepository;
    @Mock
    private DealService dealService;
    @Mock
    private TradeLifecycleService tradeLifecycleService;
    @Mock
    private NegotiationEventPublisher eventPublisher;
    @Mock
    private WaitingEventPublisher waitingEventPublisher;
    @Mock
    private Clock clock;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 15, 12, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private void setupClock() {
        given(clock.instant()).willReturn(FIXED_CLOCK.instant());
        given(clock.getZone()).willReturn(FIXED_CLOCK.getZone());
    }

    @Nested
    @DisplayName("makeOffer 메서드")
    class MakeOffer {

        @Test
        @DisplayName("성공 : 상대방이 오퍼 제출, TTL 리셋")
        void makeOfferSuccess() {
            // given
            setupClock();
            Negotiation negotiation = NegotiationFixture.createNegotiation();
            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));

            LocalDateTime beforeOffer = negotiation.getExpiresAt();

            // when - seller(1L)이 카운터 오퍼 (lastOfferedBy=buyer(2L))
            negotiationService.makeOffer(1L, 1L, 8000L);

            // then
            assertThat(negotiation.getCurrentPrice()).isEqualTo(8000L);
            assertThat(negotiation.getLastOfferedBy()).isEqualTo(1L);
            assertThat(negotiation.getOfferCount()).isEqualTo(2);
            assertThat(negotiation.getExpiresAt()).isAfterOrEqualTo(beforeOffer);
        }

        @Test
        @DisplayName("실패 : 자기 차례 아님 → NOT_YOUR_TURN")
        void makeOfferNotYourTurn() {
            // given - lastOfferedBy=buyer(2L), buyer가 다시 오퍼하면 예외
            Negotiation negotiation = NegotiationFixture.createNegotiation();
            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));

            // when & then
            assertThatThrownBy(() -> negotiationService.makeOffer(1L, 2L, 9000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NOT_YOUR_TURN);
        }

        @Test
        @DisplayName("실패 : 6회 후 FINAL_ROUND 전환 → 추가 오퍼 불가")
        void makeOfferAfterFinalRound() {
            // given
            Negotiation negotiation = NegotiationFixture.createFinalRoundNegotiation();
            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));

            // when & then - FINAL_ROUND 상태에서 makeOffer 불가
            assertThatThrownBy(() -> negotiationService.makeOffer(1L, 2L, 6500L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : NEGOTIATING 아닌 상태 → NEGOTIATION_INVALID_STATE")
        void makeOfferInvalidState() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation();
            negotiation.reject();
            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));

            // when & then
            assertThatThrownBy(() -> negotiationService.makeOffer(1L, 2L, 8000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("accept 메서드")
    class Accept {

        @Test
        @DisplayName("성공 : ACCEPTED, ExchangeRequest COMPLETED, TradePost CLOSED, 나머지 PENDING REJECTED")
        void acceptSuccess() {
            // given - lastOfferedBy=buyer(2L) → seller(1L)이 수락 가능
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 20L, 2L);
            request.select();
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            post.startNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);
            Deal deal = mock(Deal.class);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));
            given(dealService.executeTrade(negotiation)).willReturn(deal);
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            negotiationService.accept(1L, 1L);

            // then
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.COMPLETED);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CLOSED);
            verify(exchangeRequestRepository).rejectOtherPendingRequests(
                    1L, 1L, ExchangeRequestStatus.PENDING, ExchangeRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("실패 : 본인 오퍼 수락 불가 → SELF_ACCEPT")
        void acceptSelfOffer() {
            // given - lastOfferedBy=buyer(2L), buyer가 자기 오퍼 수락 시 예외
            Negotiation negotiation = NegotiationFixture.createNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));

            // when & then
            assertThatThrownBy(() -> negotiationService.accept(1L, 2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.SELF_ACCEPT);
        }

        @Test
        @DisplayName("실패 : 판매자 티켓이 NO_SHOW 상태 → SELLER_TICKET_NOT_TRADEABLE")
        void acceptSellerTicketNoShow() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            sellerTicket.call(LocalDateTime.now());
            sellerTicket.noShow();

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));

            // when & then
            assertThatThrownBy(() -> negotiationService.accept(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }
    }

    @Nested
    @DisplayName("submitFinalOffer 메서드")
    class SubmitFinalOffer {

        @Test
        @DisplayName("성공 : 양쪽 제출 완료, 구매자 >= 판매자 → 거래 성사")
        void submitFinalOfferDealReached() {
            // given
            Negotiation negotiation = NegotiationFixture.createFinalRoundNegotiation();
            ExchangeRequest request = ExchangeRequestFixture.createRequest(1L, 1L, 20L, 2L);
            request.select();
            TradePost post = TradePostFixture.createPost(1L, 10L, 1L, 1L);
            post.startNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);
            Deal deal = mock(Deal.class);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));
            given(dealService.executeTrade(negotiation)).willReturn(deal);
            given(exchangeRequestRepository.findById(1L)).willReturn(Optional.of(request));
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            negotiationService.submitFinalOffer(1L, 2L, 7000L); // 구매자 먼저 제출

            // when - 판매자 6000 <= 구매자 7000 → 거래 성사 (sellerPrice로 체결)
            negotiationService.submitFinalOffer(1L, 1L, 6000L);

            // then
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
            assertThat(negotiation.getCurrentPrice()).isEqualTo(6000L);
            assertThat(request.getStatus()).isEqualTo(ExchangeRequestStatus.COMPLETED);
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CLOSED);
        }

        @Test
        @DisplayName("성공 : 양쪽 제출 완료, 구매자 < 판매자 → 거래 불성사, EXPIRED")
        void submitFinalOfferNoDeal() {
            // given
            Negotiation negotiation = NegotiationFixture.createFinalRoundNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));

            negotiationService.submitFinalOffer(1L, 2L, 5000L); // 구매자: 5000

            // when - 판매자 8000 > 구매자 5000 → 불성사
            negotiationService.submitFinalOffer(1L, 1L, 8000L);

            // then
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.EXPIRED);
        }

        @Test
        @DisplayName("실패 : FINAL_ROUND 아닌 상태에서 submitFinalOffer → NEGOTIATION_INVALID_STATE")
        void submitFinalOfferInvalidState() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(); // NEGOTIATING 상태
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));

            // when & then - 서비스가 sellerTicket 조회 후 도메인 메서드 호출 → FINAL_ROUND 아님 예외
            assertThatThrownBy(() -> negotiationService.submitFinalOffer(1L, 2L, 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : 이미 제출한 경우 → ALREADY_SUBMITTED_FINAL_OFFER")
        void submitFinalOfferAlreadySubmitted() {
            // given
            Negotiation negotiation = NegotiationFixture.createFinalRoundNegotiation();
            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));
            given(waitingTicketRepository.findById(10L)).willReturn(Optional.of(sellerTicket));
            given(waitingTicketRepository.findById(20L)).willReturn(Optional.of(buyerTicket));

            negotiationService.submitFinalOffer(1L, 2L, 8000L); // 구매자 1차 제출

            // when & then - 구매자 재제출
            assertThatThrownBy(() -> negotiationService.submitFinalOffer(1L, 2L, 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.ALREADY_SUBMITTED_FINAL_OFFER);
        }
    }

    @Nested
    @DisplayName("reject 메서드")
    class Reject {

        @Test
        @DisplayName("성공 : REJECTED, 교환신청 취소 + 게시글 재오픈 위임")
        void rejectSuccess() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);

            given(negotiationRepository.findById(1L)).willReturn(Optional.of(negotiation));

            // when
            negotiationService.reject(1L, 1L);

            // then
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.REJECTED);
            verify(tradeLifecycleService).cancelRequestAndReopenPost(1L);
        }
    }
}
