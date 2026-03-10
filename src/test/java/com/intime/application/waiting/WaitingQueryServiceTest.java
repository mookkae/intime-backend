package com.intime.application.waiting;

import com.intime.domain.store.Store;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import com.intime.support.fixture.StoreFixture;
import com.intime.support.fixture.WaitingTicketFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("WaitingQueryService 단위 테스트")
class WaitingQueryServiceTest {

    @InjectMocks
    private WaitingQueryServiceImpl waitingQueryService;

    @Mock
    private WaitingTicketRepository waitingTicketRepository;

    @Mock
    private com.intime.domain.store.StoreRepository storeRepository;

    @Nested
    @DisplayName("getStoreQueue 메서드")
    class GetStoreQueue {

        @Test
        @DisplayName("성공 : WAITING 상태 대기열 순번 정렬 조회")
        void getStoreQueue() {
            // given
            List<WaitingTicket> tickets = List.of(
                    WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2),
                    WaitingTicketFixture.createTicket(2L, 1L, 2L, 2, 3)
            );
            given(waitingTicketRepository.findByStoreIdAndStatusOrderByPositionNumberAsc(1L, WaitingStatus.WAITING))
                    .willReturn(tickets);

            // when
            List<WaitingTicket> result = waitingQueryService.getStoreQueue(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPositionNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getMyTickets 메서드")
    class GetMyTickets {

        @Test
        @DisplayName("성공 : 활성 대기표 목록 조회")
        void getMyTickets() {
            // given
            List<WaitingTicket> tickets = List.of(
                    WaitingTicketFixture.createTicket(1L, 1L, 1L, 1, 2)
            );
            given(waitingTicketRepository.findByMemberIdAndStatusIn(1L, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED)))
                    .willReturn(tickets);

            // when
            List<WaitingTicket> result = waitingQueryService.getMyTickets(1L);

            // then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getMyPosition 메서드")
    class GetMyPosition {

        @Test
        @DisplayName("성공 : 앞에 2명, 예상 대기시간 60분")
        void getMyPosition() {
            // given
            WaitingTicket ticket = WaitingTicketFixture.createTicket(1L, 1L, 1L, 3, 2);
            Store store = StoreFixture.createStore(1L, "맛있는 식당", "서울시 강남구", 30);

            given(waitingTicketRepository.findById(1L)).willReturn(Optional.of(ticket));
            given(waitingTicketRepository.countByStoreIdAndStatusAndPositionNumberLessThan(1L, WaitingStatus.WAITING, 3))
                    .willReturn(2);
            given(storeRepository.findById(1L)).willReturn(Optional.of(store));

            // when
            WaitingPositionResponse result = waitingQueryService.getMyPosition(1L);

            // then
            assertThat(result.aheadCount()).isEqualTo(2);
            assertThat(result.estimatedWaitMinutes()).isEqualTo(60);
        }
    }
}
