package com.intime.application.waiting;

import com.intime.application.trade.TradeLifecycleService;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import com.intime.support.fixture.WaitingTicketFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PendingCallScheduler 단위 테스트")
class PendingCallSchedulerTest {

    @InjectMocks
    private PendingCallScheduler scheduler;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;
    @Mock
    private TradeLifecycleService tradeLifecycleService;
    @Mock
    private WaitingEventPublisher waitingEventPublisher;
    @Mock
    private Clock clock;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 15, 12, 10, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private void setupClock() {
        given(clock.instant()).willReturn(FIXED_CLOCK.instant());
        given(clock.getZone()).willReturn(FIXED_CLOCK.getZone());
    }

    @Test
    @DisplayName("pendingCallAt + 5분 경과, 거래 미완료 → 캐스케이드 취소, 판매자 CALLED")
    void expiredPendingCallCancelsTrade() {
        // given
        setupClock();
        WaitingTicket ticket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
        ticket.markPendingCall(FIXED_NOW.minusMinutes(6));

        given(waitingTicketRepository.findByPendingCallAtBeforeAndStatus(any(LocalDateTime.class), eq(WaitingStatus.WAITING)))
                .willReturn(List.of(ticket));

        // when
        scheduler.processPendingCallExpiry();

        // then
        verify(tradeLifecycleService).cancelActiveNegotiationByTicket(10L);
        assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CALLED);
        assertThat(ticket.getPendingCallAt()).isNull();
    }

    @Test
    @DisplayName("pendingCallAt + 5분 미경과 → 변경 없음")
    void notExpiredPendingCallNoChange() {
        // given
        setupClock();
        given(waitingTicketRepository.findByPendingCallAtBeforeAndStatus(any(LocalDateTime.class), eq(WaitingStatus.WAITING)))
                .willReturn(List.of());

        // when
        scheduler.processPendingCallExpiry();
    }
}
