package com.intime.domain.waiting;

import com.intime.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WaitingTicket 엔티티 단위 테스트")
class WaitingTicketTest {

    private WaitingTicket createDefaultTicket() {
        return WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
    }

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 대기표 생성 시 WAITING 상태")
        void createTicket() {
            WaitingTicket ticket = createDefaultTicket();

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
            WaitingTicket ticket = createDefaultTicket();

            ticket.call(LocalDateTime.now());

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CALLED);
        }

        @Test
        @DisplayName("실패 : CALLED 상태에서 call 불가")
        void callFromCalled() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            assertThatThrownBy(() -> ticket.call(LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("seat 메서드")
    class Seat {

        @Test
        @DisplayName("성공 : CALLED → SEATED")
        void seat() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            ticket.seat();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.SEATED);
        }

        @Test
        @DisplayName("실패 : WAITING 상태에서 seat 불가")
        void seatFromWaiting() {
            WaitingTicket ticket = createDefaultTicket();

            assertThatThrownBy(ticket::seat)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : WAITING → CANCELLED")
        void cancelFromWaiting() {
            WaitingTicket ticket = createDefaultTicket();

            ticket.cancel();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : CALLED → CANCELLED")
        void cancelFromCalled() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            ticket.cancel();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : SEATED 상태에서 cancel 불가")
        void cancelFromSeated() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());
            ticket.seat();

            assertThatThrownBy(ticket::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("reassignTo 메서드")
    class ReassignTo {

        @Test
        @DisplayName("성공 : memberId 교체")
        void reassignTo() {
            WaitingTicket ticket = createDefaultTicket();

            ticket.reassignTo(99L);

            assertThat(ticket.getMemberId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("성공 : swap 후 각각 상대방 memberId 보유")
        void swapMemberIds() {
            WaitingTicket sellerTicket = WaitingTicket.create(1L, 1L, 1, 2, LocalDate.of(2026, 3, 11));
            WaitingTicket buyerTicket = WaitingTicket.create(1L, 2L, 3, 2, LocalDate.of(2026, 3, 11));

            Long sellerId = sellerTicket.getMemberId();
            Long buyerId = buyerTicket.getMemberId();

            sellerTicket.reassignTo(buyerId);
            buyerTicket.reassignTo(sellerId);

            assertThat(sellerTicket.getMemberId()).isEqualTo(buyerId);
            assertThat(buyerTicket.getMemberId()).isEqualTo(sellerId);
            assertThat(sellerTicket.getPositionNumber()).isEqualTo(1);
            assertThat(buyerTicket.getPositionNumber()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("markPendingCall 메서드")
    class MarkPendingCall {

        @Test
        @DisplayName("성공 : WAITING 상태에서 pendingCallAt 설정")
        void markPendingCallFromWaiting() {
            WaitingTicket ticket = createDefaultTicket();
            LocalDateTime now = LocalDateTime.now();

            ticket.markPendingCall(now);

            assertThat(ticket.getPendingCallAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("실패 : CALLED 상태에서 markPendingCall 불가")
        void markPendingCallFromCalled() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            assertThatThrownBy(() -> ticket.markPendingCall(LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("isTradeable 메서드")
    class IsTradeable {

        @Test
        @DisplayName("성공 : WAITING 상태는 거래 가능")
        void waitingIsTradeable() {
            WaitingTicket ticket = createDefaultTicket();

            assertThat(ticket.isTradeable()).isTrue();
        }

        @Test
        @DisplayName("성공 : CALLED 상태는 거래 가능")
        void calledIsTradeable() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            assertThat(ticket.isTradeable()).isTrue();
        }

        @Test
        @DisplayName("실패 : SEATED 상태는 거래 불가")
        void seatedIsNotTradeable() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());
            ticket.seat();

            assertThat(ticket.isTradeable()).isFalse();
        }

        @Test
        @DisplayName("실패 : NO_SHOW 상태는 거래 불가")
        void noShowIsNotTradeable() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());
            ticket.noShow();

            assertThat(ticket.isTradeable()).isFalse();
        }
    }

    @Nested
    @DisplayName("noShow 메서드")
    class NoShow {

        @Test
        @DisplayName("성공 : CALLED → NO_SHOW")
        void noShow() {
            WaitingTicket ticket = createDefaultTicket();
            ticket.call(LocalDateTime.now());

            ticket.noShow();

            assertThat(ticket.getStatus()).isEqualTo(WaitingStatus.NO_SHOW);
        }

        @Test
        @DisplayName("실패 : WAITING 상태에서 noShow 불가")
        void noShowFromWaiting() {
            WaitingTicket ticket = createDefaultTicket();

            assertThatThrownBy(ticket::noShow)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(WaitingCode.WAITING_INVALID_STATE);
        }
    }
}
