package com.intime.domain.negotiation;

import com.intime.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Negotiation 엔티티 단위 테스트")
class NegotiationTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 3, 15, 12, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(
            FIXED_NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    private Negotiation createNegotiation() {
        return Negotiation.create(1L, 1L, 2L, 10L, 20L, 10000L, FIXED_CLOCK);
    }

    @Nested
    @DisplayName("create 팩토리 메서드")
    class Create {

        @Test
        @DisplayName("성공 : 생성 시 NEGOTIATING, offerCount=1, lastOfferedBy=buyerId")
        void createNegotiation() {
            Negotiation negotiation = Negotiation.create(1L, 1L, 2L, 10L, 20L, 10000L, FIXED_CLOCK);

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.NEGOTIATING);
            assertThat(negotiation.getCurrentPrice()).isEqualTo(10000L);
            assertThat(negotiation.getLastOfferedBy()).isEqualTo(2L);
            assertThat(negotiation.getOfferCount()).isEqualTo(1);
            assertThat(negotiation.getExpiresAt()).isEqualTo(FIXED_NOW.plusMinutes(5));
        }
    }

    @Nested
    @DisplayName("makeOffer 메서드")
    class MakeOffer {

        @Test
        @DisplayName("성공 : 상대방이 오퍼, 가격/카운트/TTL 갱신")
        void makeOfferSuccess() {
            Negotiation negotiation = createNegotiation();

            negotiation.makeOffer(1L, 8000L);

            assertThat(negotiation.getCurrentPrice()).isEqualTo(8000L);
            assertThat(negotiation.getLastOfferedBy()).isEqualTo(1L);
            assertThat(negotiation.getOfferCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("실패 : 자기 차례 아닌 경우 예외")
        void makeOfferNotYourTurn() {
            Negotiation negotiation = createNegotiation();
            // lastOfferedBy=buyer(2L), buyer가 또 오퍼하면 예외

            assertThatThrownBy(() -> negotiation.makeOffer(2L, 9000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NOT_YOUR_TURN);
        }

        @Test
        @DisplayName("실패 : NEGOTIATING 아닌 상태에서 예외")
        void makeOfferInvalidState() {
            Negotiation negotiation = createNegotiation();
            negotiation.reject();

            assertThatThrownBy(() -> negotiation.makeOffer(1L, 8000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }

        @Test
        @DisplayName("6회 오퍼 도달 시 FINAL_ROUND 자동 전환")
        void makeOfferTransitionsToFinalRound() {
            Negotiation negotiation = createNegotiation();
            // offerCount=1(create), 2, 3, 4, 5, 6 → FINAL_ROUND
            negotiation.makeOffer(1L, 9000L); // 2
            negotiation.makeOffer(2L, 8500L); // 3
            negotiation.makeOffer(1L, 8000L); // 4
            negotiation.makeOffer(2L, 7500L); // 5

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.NEGOTIATING);

            negotiation.makeOffer(1L, 7000L); // 6 → FINAL_ROUND

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.FINAL_ROUND);
            assertThat(negotiation.getOfferCount()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("accept 메서드")
    class Accept {

        @Test
        @DisplayName("성공 : 상대방이 수락 → ACCEPTED")
        void acceptSuccess() {
            Negotiation negotiation = createNegotiation();
            // lastOfferedBy=buyer(2L) → seller(1L)가 수락

            negotiation.accept(1L);

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
        }

        @Test
        @DisplayName("실패 : 본인 오퍼 수락 불가 → SELF_ACCEPT 예외")
        void acceptSelfOffer() {
            Negotiation negotiation = createNegotiation();
            // lastOfferedBy=buyer(2L) → buyer(2L)가 자기 오퍼 수락 시도

            assertThatThrownBy(() -> negotiation.accept(2L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.SELF_ACCEPT);
        }

        @Test
        @DisplayName("실패 : NEGOTIATING 아닌 상태에서 예외")
        void acceptInvalidState() {
            Negotiation negotiation = createNegotiation();
            negotiation.reject();

            assertThatThrownBy(() -> negotiation.accept(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("reject 메서드")
    class Reject {

        @Test
        @DisplayName("성공 : NEGOTIATING → REJECTED")
        void rejectSuccess() {
            Negotiation negotiation = createNegotiation();

            negotiation.reject();

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.REJECTED);
        }

        @Test
        @DisplayName("실패 : REJECTED 상태에서 다시 reject 예외")
        void rejectInvalidState() {
            Negotiation negotiation = createNegotiation();
            negotiation.reject();

            assertThatThrownBy(negotiation::reject)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("submitFinalOffer 메서드")
    class SubmitFinalOffer {

        private Negotiation createFinalRoundNegotiation() {
            Negotiation negotiation = createNegotiation();
            negotiation.makeOffer(1L, 9000L);
            negotiation.makeOffer(2L, 8500L);
            negotiation.makeOffer(1L, 8000L);
            negotiation.makeOffer(2L, 7500L);
            negotiation.makeOffer(1L, 7000L); // 6 → FINAL_ROUND
            return negotiation;
        }

        @Test
        @DisplayName("한쪽만 제출 → false, 상태 유지")
        void submitOneSide() {
            Negotiation negotiation = createFinalRoundNegotiation();

            boolean result = negotiation.submitFinalOffer(2L, 7000L);

            assertThat(result).isFalse();
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.FINAL_ROUND);
            assertThat(negotiation.getBuyerFinalPrice()).isEqualTo(7000L);
            assertThat(negotiation.getSellerFinalPrice()).isNull();
        }

        @Test
        @DisplayName("양쪽 제출, buyerPrice >= sellerPrice → 거래 성사 (sellerPrice로 체결)")
        void submitBothSidesDealReached() {
            Negotiation negotiation = createFinalRoundNegotiation();
            negotiation.submitFinalOffer(2L, 7000L); // buyer

            boolean result = negotiation.submitFinalOffer(1L, 6000L); // seller

            assertThat(result).isTrue();
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.ACCEPTED);
            assertThat(negotiation.getCurrentPrice()).isEqualTo(6000L);
        }

        @Test
        @DisplayName("양쪽 제출, buyerPrice < sellerPrice → 거래 불성사, FAILED")
        void submitBothSidesNoDeal() {
            Negotiation negotiation = createFinalRoundNegotiation();
            negotiation.submitFinalOffer(2L, 5000L); // buyer

            boolean result = negotiation.submitFinalOffer(1L, 8000L); // seller

            assertThat(result).isFalse();
            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.FAILED);
        }

        @Test
        @DisplayName("실패 : FINAL_ROUND 아닌 상태에서 예외")
        void submitFinalOfferInvalidState() {
            Negotiation negotiation = createNegotiation(); // NEGOTIATING

            assertThatThrownBy(() -> negotiation.submitFinalOffer(2L, 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }

        @Test
        @DisplayName("실패 : 참가자가 아닌 경우 예외")
        void submitFinalOfferNotParticipant() {
            Negotiation negotiation = createFinalRoundNegotiation();

            assertThatThrownBy(() -> negotiation.submitFinalOffer(99L, 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_NOT_PARTICIPANT);
        }

        @Test
        @DisplayName("실패 : 이미 제출한 경우 재제출 예외")
        void submitFinalOfferAlreadySubmitted() {
            Negotiation negotiation = createFinalRoundNegotiation();
            negotiation.submitFinalOffer(2L, 7000L);

            assertThatThrownBy(() -> negotiation.submitFinalOffer(2L, 6000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.ALREADY_SUBMITTED_FINAL_OFFER);
        }
    }

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @Test
        @DisplayName("성공 : NEGOTIATING → CANCELLED")
        void cancelFromNegotiating() {
            Negotiation negotiation = createNegotiation();

            negotiation.cancel();

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 : FINAL_ROUND → CANCELLED")
        void cancelFromFinalRound() {
            Negotiation negotiation = createNegotiation();
            negotiation.makeOffer(1L, 9000L);
            negotiation.makeOffer(2L, 8500L);
            negotiation.makeOffer(1L, 8000L);
            negotiation.makeOffer(2L, 7500L);
            negotiation.makeOffer(1L, 7000L);

            negotiation.cancel();

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 : ACCEPTED → cancel 예외")
        void cancelFromAccepted() {
            Negotiation negotiation = createNegotiation();
            negotiation.accept(1L);

            assertThatThrownBy(negotiation::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("expire 메서드")
    class Expire {

        @Test
        @DisplayName("성공 : NEGOTIATING → EXPIRED")
        void expireFromNegotiating() {
            Negotiation negotiation = createNegotiation();

            negotiation.expire();

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.EXPIRED);
        }

        @Test
        @DisplayName("성공 : FINAL_ROUND → EXPIRED")
        void expireFromFinalRound() {
            Negotiation negotiation = createNegotiation();
            negotiation.makeOffer(1L, 9000L);
            negotiation.makeOffer(2L, 8500L);
            negotiation.makeOffer(1L, 8000L);
            negotiation.makeOffer(2L, 7500L);
            negotiation.makeOffer(1L, 7000L);

            negotiation.expire();

            assertThat(negotiation.getStatus()).isEqualTo(NegotiationStatus.EXPIRED);
        }

        @Test
        @DisplayName("실패 : REJECTED → expire 예외")
        void expireFromRejected() {
            Negotiation negotiation = createNegotiation();
            negotiation.reject();

            assertThatThrownBy(negotiation::expire)
                    .isInstanceOf(BusinessException.class)
                    .extracting("baseCode")
                    .isEqualTo(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }

    @Nested
    @DisplayName("isParticipant 메서드")
    class IsParticipant {

        @Test
        @DisplayName("sellerId → true")
        void sellerIsParticipant() {
            Negotiation negotiation = createNegotiation();

            assertThat(negotiation.isParticipant(1L)).isTrue();
        }

        @Test
        @DisplayName("buyerId → true")
        void buyerIsParticipant() {
            Negotiation negotiation = createNegotiation();

            assertThat(negotiation.isParticipant(2L)).isTrue();
        }

        @Test
        @DisplayName("제3자 → false")
        void otherIsNotParticipant() {
            Negotiation negotiation = createNegotiation();

            assertThat(negotiation.isParticipant(99L)).isFalse();
        }
    }
}
