package com.intime.application.trade;

import com.intime.application.trade.fixture.ExchangeRequestFixture;
import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.ExchangeRequestStatus;
import com.intime.support.TestReflectionUtils;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRequestScheduler 단위 테스트")
class ExchangeRequestSchedulerTest {

    @InjectMocks
    private ExchangeRequestScheduler scheduler;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

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

    @Test
    @DisplayName("성공 : 만료 시각 지난 PENDING → EXPIRED 처리")
    void expireExpiredRequests() {
        // given
        setupClock();
        ExchangeRequest expiredRequest = ExchangeRequestFixture.createRequest(1L, 1L, 2L, 2L);
        TestReflectionUtils.setField(expiredRequest, "expiresAt", FIXED_NOW.minusMinutes(1));
        given(exchangeRequestRepository.findByStatusAndExpiresAtBefore(ExchangeRequestStatus.PENDING, FIXED_NOW))
                .willReturn(List.of(expiredRequest));

        // when
        scheduler.expireRequests();

        // then
        assertThat(expiredRequest.getStatus()).isEqualTo(ExchangeRequestStatus.EXPIRED);
    }

    @Test
    @DisplayName("성공 : 아직 유효한 PENDING → 변경 없음")
    void doNotExpireValidRequests() {
        // given
        setupClock();
        given(exchangeRequestRepository.findByStatusAndExpiresAtBefore(ExchangeRequestStatus.PENDING, FIXED_NOW))
                .willReturn(List.of());

        // when
        scheduler.expireRequests();

        // then - no exceptions, no state changes
    }
}
