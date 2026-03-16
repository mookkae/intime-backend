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
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingNoShowScheduler 단위 테스트")
class WaitingNoShowSchedulerTest {

    @InjectMocks
    private WaitingNoShowScheduler scheduler;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;
    @Mock
    private TradeLifecycleService tradeLifecycleService;
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
    @DisplayName("calledAt + 5분 경과 → 캐스케이드 취소 후 NO_SHOW 처리")
    void expiredCalledTicketMarkedNoShow() {
        // given
        setupClock();
        WaitingTicket ticket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
        ticket.call(FIXED_NOW.minusMinutes(6));

        given(waitingTicketRepository.findByStatusAndCalledAtBefore(eq(WaitingStatus.CALLED), any(LocalDateTime.class)))
                .willReturn(List.of(ticket));

        // when
        scheduler.markNoShowExpired();

        // then
        verify(tradeLifecycleService).cancelActiveNegotiationByTicket(10L);
        assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
    }

    @Test
    @DisplayName("calledAt + 5분 미경과 → 변경 없음")
    void notExpiredCalledTicketNoChange() {
        // given
        setupClock();
        given(waitingTicketRepository.findByStatusAndCalledAtBefore(eq(WaitingStatus.CALLED), any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        scheduler.markNoShowExpired();

        // then
        verifyNoInteractions(tradeLifecycleService);
    }

    @Test
    @DisplayName("배치 중 1건 실패 → 해당 건 스킵, 나머지 정상 처리")
    void singleFailureIsolatedFromBatch() {
        // given
        setupClock();
        WaitingTicket failTicket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
        failTicket.call(FIXED_NOW.minusMinutes(6));
        WaitingTicket successTicket = WaitingTicketFixture.createTicket(20L, 1L, 2L, 2, 2);
        successTicket.call(FIXED_NOW.minusMinutes(6));

        given(waitingTicketRepository.findByStatusAndCalledAtBefore(eq(WaitingStatus.CALLED), any(LocalDateTime.class)))
                .willReturn(List.of(failTicket, successTicket));
        willThrow(new RuntimeException("DB error"))
                .given(tradeLifecycleService).cancelActiveNegotiationByTicket(10L);

        // when - 예외 발생해도 전체 스케줄러가 죽지 않음
        scheduler.markNoShowExpired();

        // then
        assertThat(failTicket.getStatus()).isEqualTo(WaitingStatus.CALLED); // 실패한 건 상태 유지
        assertThat(successTicket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW); // 나머지 정상 처리
    }
}
