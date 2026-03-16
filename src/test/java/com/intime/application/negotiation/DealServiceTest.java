package com.intime.application.negotiation;

import com.intime.application.negotiation.fixture.NegotiationFixture;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Deal;
import com.intime.domain.negotiation.DealRepository;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationCode;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DealService 단위 테스트")
class DealServiceTest {

    @InjectMocks
    private DealServiceImpl dealService;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

    @Mock
    private DealRepository dealRepository;

    @Nested
    @DisplayName("executeTrade 메서드")
    class ExecuteTrade {

        @Test
        @DisplayName("성공 : memberId swap - 판매자 티켓에 구매자 ID, 구매자 티켓에 판매자 ID")
        void executeTradeSwapMemberIds() {
            // given
            // sellerId=1, buyerId=2, sellerTicketId=10, buyerTicketId=20
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);

            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 3, 2);

            given(waitingTicketRepository.findByIdsWithLock(List.of(10L, 20L)))
                    .willReturn(List.of(sellerTicket, buyerTicket));
            given(dealRepository.save(any(Deal.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            dealService.executeTrade(negotiation);

            // then
            assertThat(sellerTicket.getMemberId()).isEqualTo(2L);
            assertThat(buyerTicket.getMemberId()).isEqualTo(1L);
            assertThat(sellerTicket.getPositionNumber()).isEqualTo(1);
            assertThat(buyerTicket.getPositionNumber()).isEqualTo(3);
        }

        @Test
        @DisplayName("성공 : Deal 기록 생성, 합의 가격 기록")
        void executeTradeCreatesDealRecord() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);

            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 3, 2);

            given(waitingTicketRepository.findByIdsWithLock(List.of(10L, 20L)))
                    .willReturn(List.of(sellerTicket, buyerTicket));
            given(dealRepository.save(any(Deal.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            Deal deal = dealService.executeTrade(negotiation);

            // then
            assertThat(deal.getAgreedPrice()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("실패 : 판매자 티켓이 NO_SHOW 상태 → SELLER_TICKET_NOT_TRADEABLE 예외")
        void executeTradeSellerTicketNoShow() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);

            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            sellerTicket.call(LocalDateTime.now());
            sellerTicket.noShow();
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 3, 2);

            given(waitingTicketRepository.findByIdsWithLock(List.of(10L, 20L)))
                    .willReturn(List.of(sellerTicket, buyerTicket));

            // when & then
            assertThatThrownBy(() -> dealService.executeTrade(negotiation))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }

        @Test
        @DisplayName("실패 : 구매자 티켓이 NO_SHOW 상태 → BUYER_TICKET_NOT_TRADEABLE 예외")
        void executeTradeBuyerTicketNoShow() {
            // given
            Negotiation negotiation = NegotiationFixture.createNegotiation(
                    1L, 1L, 1L, 2L, 10L, 20L, 10000L);

            WaitingTicket sellerTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
            WaitingTicket buyerTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 3, 2);
            buyerTicket.call(LocalDateTime.now());
            buyerTicket.noShow();

            given(waitingTicketRepository.findByIdsWithLock(List.of(10L, 20L)))
                    .willReturn(List.of(sellerTicket, buyerTicket));

            // when & then
            assertThatThrownBy(() -> dealService.executeTrade(negotiation))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.BUYER_TICKET_NOT_TRADEABLE);
        }
    }
}
