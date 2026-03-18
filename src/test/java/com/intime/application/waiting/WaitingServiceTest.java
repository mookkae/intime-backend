package com.intime.application.waiting;

import com.intime.application.trade.TradePostEventPublisher;
import com.intime.application.waiting.dto.WaitingRegisterCommand;
import com.intime.application.waiting.dto.WaitingTicketInfo;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.TradePostRepository;
import com.intime.domain.trade.TradePostStatus;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingStatus;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingService 단위 테스트")
class WaitingServiceTest {

    @InjectMocks
    private WaitingServiceImpl waitingService;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

    @Mock
    private TradePostRepository tradePostRepository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private NegotiationRepository negotiationRepository;

    @Mock
    private WaitingEventPublisher waitingEventPublisher;

    @Mock
    private TradePostEventPublisher tradePostEventPublisher;

    @Mock
    private Clock clock;

    private static final LocalDate FIXED_DATE = LocalDate.of(2026, 3, 11);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_DATE.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private void setupClock() {
        given(clock.instant()).willReturn(FIXED_CLOCK.instant());
        given(clock.getZone()).willReturn(FIXED_CLOCK.getZone());
    }

    @Nested
    @DisplayName("register 메서드")
    class Register {

        @Test
        @DisplayName("성공 : 첫 번째 대기표 등록 시 순번 1")
        void registerFirst() {
            // given
            setupClock();
            WaitingRegisterCommand command = new WaitingRegisterCommand(1L, 1L, 2);
            given(waitingTicketRepository.existsByMemberIdAndStoreIdAndWaitingDateAndStatusIn(
                    1L, 1L, FIXED_DATE, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)))
                    .willReturn(false);
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.empty());
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            WaitingTicketInfo result = waitingService.register(command);

            // then
            assertThat(result.positionNumber()).isEqualTo(1);
            assertThat(result.status()).isEqualTo(WaitingStatus.WAITING);
        }

        @Test
        @DisplayName("성공 : 기존 대기표가 있으면 다음 순번 발급")
        void registerNext() {
            // given
            setupClock();
            WaitingRegisterCommand command = new WaitingRegisterCommand(1L, 1L, 2);
            WaitingTicket existing = WaitingTicketFixture.createTicket(1L, 1L, 2L, 3, 2);
            given(waitingTicketRepository.existsByMemberIdAndStoreIdAndWaitingDateAndStatusIn(
                    1L, 1L, FIXED_DATE, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)))
                    .willReturn(false);
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.of(existing));
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            WaitingTicketInfo result = waitingService.register(command);

            // then
            assertThat(result.positionNumber()).isEqualTo(4);
        }

        @Test
        @DisplayName("실패 : 동일 가게 WAITING/CALLED 상태 중복 등록 시 예외")
        void registerDuplicate() {
            // given
            setupClock();
            WaitingRegisterCommand command = new WaitingRegisterCommand(1L, 1L, 2);
            given(waitingTicketRepository.existsByMemberIdAndStoreIdAndWaitingDateAndStatusIn(
                    1L, 1L, FIXED_DATE, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> waitingService.register(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_DUPLICATE);
        }

        @Test
        @DisplayName("실패 : 순번 충돌 시 등록 실패 예외")
        void registerConflict() {
            // given
            setupClock();
            WaitingRegisterCommand command = new WaitingRegisterCommand(1L, 1L, 2);
            given(waitingTicketRepository.existsByMemberIdAndStoreIdAndWaitingDateAndStatusIn(
                    1L, 1L, FIXED_DATE, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)))
                    .willReturn(false);
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.empty());
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willThrow(new DataIntegrityViolationException("Duplicate entry"));

            // when & then
            assertThatThrownBy(() -> waitingService.register(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_REGISTER_FAILED);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : 본인 대기표 취소")
        void cancel() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when
            waitingService.cancel(1L, 1L);

            // then
            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : 타인의 대기표 취소 시 예외")
        void cancelNotOwner() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when & then
            assertThatThrownBy(() -> waitingService.cancel(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_NOT_OWNER);
        }
    }

    @Nested
    @DisplayName("callNext 메서드")
    class CallNext {

        @Test
        @DisplayName("성공 : 가장 작은 순번 호출 (거래 없음)")
        void callNext() {
            // given
            setupClock();
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            given(waitingTicketRepository.findTopByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(Optional.of(ticket));
            given(tradePostRepository.findByWaitingTicketIdAndStatus(1L, TradePostStatus.OPEN))
                    .willReturn(Optional.empty());
            given(negotiationRepository.findBySellerTicketIdAndStatusIn(eq(1L), any()))
                    .willReturn(Optional.empty());

            // when
            WaitingTicketInfo result = waitingService.callNext(1L);

            // then
            assertThat(result.status()).isEqualTo(WaitingStatus.CALLED);
        }

        @Test
        @DisplayName("실패 : 대기 중인 순번 없음")
        void noOneWaiting() {
            // given
            given(waitingTicketRepository.findTopByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingService.callNext(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_NO_ONE_WAITING);
        }

        @Test
        @DisplayName("성공 : 활성 협상 중인 티켓 → PENDING_CALL 설정, CALLED 아님")
        void callNextWithActiveNegotiation() {
            // given
            setupClock();
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            given(waitingTicketRepository.findTopByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(Optional.of(ticket));
            given(tradePostRepository.findByWaitingTicketIdAndStatus(1L, TradePostStatus.OPEN))
                    .willReturn(Optional.empty());
            given(negotiationRepository.findBySellerTicketIdAndStatusIn(eq(1L), any()))
                    .willReturn(Optional.of(mock(Negotiation.class)));

            // when
            WaitingTicketInfo result = waitingService.callNext(1L);

            // then
            assertThat(result.status()).isEqualTo(WaitingStatus.WAITING);
        }
    }

    @Nested
    @DisplayName("confirmSeated 메서드")
    class ConfirmSeated {

        @Test
        @DisplayName("성공 : 착석 처리")
        void confirmSeated() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            ticket.call(LocalDateTime.now());
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when
            waitingService.confirmSeated(1L);

            // then
            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.SEATED);
        }
    }

    @Nested
    @DisplayName("markNoShow 메서드")
    class MarkNoShow {

        @Test
        @DisplayName("성공 : 노쇼 처리")
        void markNoShow() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            ticket.call(LocalDateTime.now());
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when
            waitingService.markNoShow(1L);

            // then
            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
        }
    }
}
