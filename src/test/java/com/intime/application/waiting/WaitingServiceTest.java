package com.intime.application.waiting;

import com.intime.common.exception.BusinessException;
import com.intime.domain.waiting.*;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingService 단위 테스트")
class WaitingServiceTest {

    @InjectMocks
    private WaitingServiceImpl waitingService;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

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
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.empty());
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            WaitingTicket result = waitingService.register(1L, 1L, 2);

            // then
            assertThat(result.getPositionNumber()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo(WaitingStatus.WAITING);
            verify(waitingTicketRepository).save(any(WaitingTicket.class));
        }

        @Test
        @DisplayName("성공 : 기존 대기표가 있으면 다음 순번 발급")
        void registerNext() {
            // given
            setupClock();
            WaitingTicket existing = WaitingTicketFixture.createTicket(1L, 1L, 2L, 3, 2);
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.of(existing));
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            WaitingTicket result = waitingService.register(1L, 1L, 2);

            // then
            assertThat(result.getPositionNumber()).isEqualTo(4);
        }

        @Test
        @DisplayName("실패 : 순번 충돌 시 등록 실패 예외")
        void registerConflict() {
            // given
            setupClock();
            given(waitingTicketRepository.findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(1L, FIXED_DATE))
                    .willReturn(Optional.empty());
            given(waitingTicketRepository.save(any(WaitingTicket.class)))
                    .willThrow(new DataIntegrityViolationException("Duplicate entry"));

            // when & then
            assertThatThrownBy(() -> waitingService.register(1L, 1L, 2))
                    .isInstanceOf(BusinessException.class);
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
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("callNext 메서드")
    class CallNext {

        @Test
        @DisplayName("성공 : 가장 작은 순번 호출")
        void callNext() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2);
            given(waitingTicketRepository.findTopByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(Optional.of(ticket));

            // when
            WaitingTicket result = waitingService.callNext(1L);

            // then
            assertThat(result.getStatus()).isEqualTo(WaitingStatus.CALLED);
        }

        @Test
        @DisplayName("실패 : 대기 중인 순번 없음")
        void noOneWaiting() {
            // given
            given(waitingTicketRepository.findTopByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingService.callNext(1L))
                    .isInstanceOf(BusinessException.class);
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
            ticket.call();
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
            ticket.call();
            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));

            // when
            waitingService.markNoShow(1L);

            // then
            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
        }
    }
}
