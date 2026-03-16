package com.intime.application.waiting;

import com.intime.application.trade.TradeLifecycleService;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingBatchProcessor 단위 테스트")
class WaitingBatchProcessorTest {

    @InjectMocks
    private WaitingBatchProcessor processor;

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
    @DisplayName("processPendingCall: 거래 취소 → pendingCall 해제 → CALLED 전이 → 이벤트 발행")
    void processPendingCall() {
        // given
        setupClock();
        WaitingTicket ticket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
        ticket.markPendingCall(FIXED_NOW.minusMinutes(6));

        // when
        processor.processPendingCall(ticket);

        // then
        verify(tradeLifecycleService).cancelActiveNegotiationByTicket(10L);
        assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CALLED);
        assertThat(ticket.getPendingCallAt()).isNull();
        verify(waitingEventPublisher).publishCalled(10L);
    }

    @Test
    @DisplayName("processNoShow: 거래 취소 → NO_SHOW 전이")
    void processNoShow() {
        // given
        WaitingTicket ticket = WaitingTicketFixture.createTicket(10L, 1L, 1L, 1, 2);
        ticket.call(FIXED_NOW.minusMinutes(6));

        // when
        processor.processNoShow(ticket);

        // then
        verify(tradeLifecycleService).cancelActiveNegotiationByTicket(10L);
        assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
    }
}
