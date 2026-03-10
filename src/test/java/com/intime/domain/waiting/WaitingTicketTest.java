package com.intime.domain.waiting;

import com.intime.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WaitingTicket 엔티티 단위 테스트")
class WaitingTicketTest {

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 대기표 생성 시 WAITING 상태")
        void createTicket() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));

            assertThat(ticket.getStoreId()).isEqualTo(1L);
            assertThat(ticket.getMemberId()).isEqualTo(1L);
            assertThat(ticket.getPositionNumber()).isEqualTo(1);
            assertThat(ticket.getPartySize()).isEqualTo(2);
            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.WAITING);
            assertThat(ticket.getWaitingDate()).isEqualTo(LocalDate.of(2026, 3, 11));
        }
    }

    @Nested
    @DisplayName("call 메서드")
    class Call {

        @Test
        @DisplayName("성공 : WAITING → CALLED")
        void call() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));

            ticket.call();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CALLED);
        }

        @Test
        @DisplayName("실패 : CALLED 상태에서 call 불가")
        void callFromCalled() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            ticket.call();

            assertThatThrownBy(ticket::call)
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("seat 메서드")
    class Seat {

        @Test
        @DisplayName("성공 : CALLED → SEATED")
        void seat() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            ticket.call();

            ticket.seat();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.SEATED);
        }

        @Test
        @DisplayName("실패 : WAITING 상태에서 seat 불가")
        void seatFromWaiting() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));

            assertThatThrownBy(ticket::seat)
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : WAITING → CANCELLED")
        void cancelFromWaiting() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));

            ticket.cancel();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : CALLED → CANCELLED")
        void cancelFromCalled() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            ticket.call();

            ticket.cancel();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : SEATED 상태에서 cancel 불가")
        void cancelFromSeated() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            ticket.call();
            ticket.seat();

            assertThatThrownBy(ticket::cancel)
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("noShow 메서드")
    class NoShow {

        @Test
        @DisplayName("성공 : CALLED → NO_SHOW")
        void noShow() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            ticket.call();

            ticket.noShow();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
        }

        @Test
        @DisplayName("실패 : WAITING 상태에서 noShow 불가")
        void noShowFromWaiting() {
            WaitingTicket ticket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));

            assertThatThrownBy(ticket::noShow)
                    .isInstanceOf(BusinessException.class);
        }
    }
}
