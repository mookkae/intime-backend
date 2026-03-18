package com.intime.application.trade;

import com.intime.application.trade.dto.TradePostInfo;
import com.intime.application.trade.dto.TradePostRegisterCommand;
import com.intime.application.trade.fixture.TradePostFixture;
import com.intime.common.exception.BusinessException;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TradePostService 단위 테스트")
class TradePostServiceTest {

    @InjectMocks
    private TradePostServiceImpl tradePostService;

    @Mock
    private TradePostRepository tradePostRepository;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private TradePostEventPublisher tradePostEventPublisher;

    @Nested
    @DisplayName("register 메서드")
    class Register {

        @Test
        @DisplayName("성공 : WAITING 티켓 + 본인 소유로 판매 등록")
        void registerSuccess() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            TradePostRegisterCommand command = new TradePostRegisterCommand(1L, 1L, null);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));
            given(tradePostRepository.existsByWaitingTicketIdAndStatus(1L, TradePostStatus.OPEN)).willReturn(false);
            given(tradePostRepository.save(any(TradePost.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            TradePostInfo result = tradePostService.register(command);

            // then
            assertThat(result.status()).isEqualTo(TradePostStatus.OPEN);
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.storeId()).isEqualTo(ticket.getStoreId());
        }

        @Test
        @DisplayName("실패 : WAITING 아닌 티켓으로 등록 시 예외")
        void registerNonWaitingTicket() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            ticket.call(LocalDateTime.now());
            TradePostRegisterCommand command = new TradePostRegisterCommand(1L, 1L, null);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when & then
            assertThatThrownBy(() -> tradePostService.register(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 : 본인 티켓 아닌 경우 예외")
        void registerNotOwner() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            TradePostRegisterCommand command = new TradePostRegisterCommand(1L, 999L, null);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when & then
            assertThatThrownBy(() -> tradePostService.register(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 : 이미 OPEN 포스트 있는 티켓 재등록 시 예외")
        void registerDuplicate() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            TradePostRegisterCommand command = new TradePostRegisterCommand(1L, 1L, null);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));
            given(tradePostRepository.existsByWaitingTicketIdAndStatus(1L, TradePostStatus.OPEN)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> tradePostService.register(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("withdraw 메서드")
    class Withdraw {

        @Test
        @DisplayName("성공 : 본인 포스트 철회")
        void withdrawSuccess() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 1L, 1L, 1L);
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            tradePostService.withdraw(1L, 1L);

            // then
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : 타인 포스트 철회 시 예외")
        void withdrawNotOwner() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 1L, 1L, 1L);
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> tradePostService.withdraw(1L, 999L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("성공 : OPEN 포스트 철회 시 PENDING 신청 일괄 취소 + WebSocket 발행")
        void withdrawCancelsPendingRequestsAndPublishesEvent() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 1L, 1L, 1L);
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            tradePostService.withdraw(1L, 1L);

            // then
            assertThat(post.getStatus()).isEqualTo(TradePostStatus.CANCELLED);
            verify(exchangeRequestRepository).cancelAllPendingByTradePostId(
                    eq(1L), eq(ExchangeRequestStatus.PENDING), eq(ExchangeRequestStatus.CANCELLED));
            verify(tradePostEventPublisher).publishPostCancelled(1L);
        }

        @Test
        @DisplayName("실패 : NEGOTIATING 상태 포스트 철회 시 예외")
        void withdrawNegotiatingPost() {
            // given
            TradePost post = TradePostFixture.createPost(1L, 1L, 1L, 1L);
            post.startNegotiation();
            given(tradePostRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> tradePostService.withdraw(1L, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }
}
