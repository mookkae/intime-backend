package com.intime.application.negotiation;

import com.intime.application.negotiation.fixture.NegotiationFixture;
import com.intime.application.trade.TradeLifecycleService;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("NegotiationScheduler 단위 테스트")
class NegotiationSchedulerTest {

    @InjectMocks
    private NegotiationScheduler scheduler;

    @Mock
    private NegotiationRepository negotiationRepository;
    @Mock
    private TradeLifecycleService tradeLifecycleService;
    @Mock
    private NegotiationEventPublisher eventPublisher;
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

    @Test
    @DisplayName("만료된 NEGOTIATING → EXPIRED, 교환신청 취소 + 게시글 재오픈 위임")
    void expireNegotiations() {
        // given
        setupClock();
        Negotiation negotiation = NegotiationFixture.createNegotiation(
                1L, 1L, 1L, 2L, 10L, 20L, 10000L);

        given(negotiationRepository.findByStatusInAndExpiresAtBefore(
                eq(List.of(NegotiationStatus.NEGOTIATING, NegotiationStatus.FINAL_ROUND)), any(LocalDateTime.class)))
                .willReturn(List.of(negotiation));

        // when
        scheduler.expireNegotiations();

        // then
        assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.EXPIRED);
        verify(tradeLifecycleService).cancelRequestAndReopenPost(1L);
    }

    @Test
    @DisplayName("유효한 NEGOTIATING → 변경 없음")
    void noExpiredNegotiations() {
        // given
        setupClock();
        given(negotiationRepository.findByStatusInAndExpiresAtBefore(
                eq(List.of(NegotiationStatus.NEGOTIATING, NegotiationStatus.FINAL_ROUND)), any(LocalDateTime.class)))
                .willReturn(List.of());

        // when
        scheduler.expireNegotiations();

        // then
        verifyNoInteractions(tradeLifecycleService);
        verifyNoInteractions(eventPublisher);
    }
}
