package com.intime.domain.negotiation;

import com.intime.common.BaseTimeEntity;
import com.intime.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "negotiation",
        uniqueConstraints = @UniqueConstraint(columnNames = "exchange_request_id"),
        indexes = {
                @Index(name = "idx_negotiation_exchange_request", columnList = "exchange_request_id"),
                @Index(name = "idx_negotiation_seller_ticket_status", columnList = "seller_ticket_id, status"),
                @Index(name = "idx_negotiation_status_expires", columnList = "status, expires_at")
        }
)
public class Negotiation extends BaseTimeEntity {

    public static final int MAX_OFFERS = 6;
    public static final int NEGOTIATION_TTL_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long exchangeRequestId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerTicketId;

    @Column(nullable = false)
    private Long buyerTicketId;

    @Column(nullable = false)
    private Long currentPrice;

    @Column(nullable = false)
    private Long lastOfferedBy;

    @Column(nullable = false)
    private int offerCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NegotiationStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 봉인 입찰 최종가 (FINAL_ROUND에서 사용)
    @Column
    private Long sellerFinalPrice;

    @Column
    private Long buyerFinalPrice;

    @Builder
    private Negotiation(Long exchangeRequestId, Long sellerId, Long buyerId,
                        Long sellerTicketId, Long buyerTicketId,
                        Long buyerOfferPrice, LocalDateTime expiresAt) {
        this.exchangeRequestId = exchangeRequestId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.sellerTicketId = sellerTicketId;
        this.buyerTicketId = buyerTicketId;
        this.currentPrice = buyerOfferPrice;
        this.lastOfferedBy = buyerId;  // 구매자가 첫 오퍼
        this.offerCount = 1;
        this.status = NegotiationStatus.NEGOTIATING;
        this.expiresAt = expiresAt;
    }

    /**
     * 구매자의 offerPrice를 offer 1로 삼아 협상을 시작
     * selectBuyer() 시점에 자동 생성
     */
    public static Negotiation create(Long exchangeRequestId, Long sellerId, Long buyerId,
                                     Long sellerTicketId, Long buyerTicketId,
                                     Long buyerOfferPrice, Clock clock) {
        return new Negotiation(exchangeRequestId, sellerId, buyerId,
                sellerTicketId, buyerTicketId, buyerOfferPrice,
                LocalDateTime.now(clock).plusMinutes(NEGOTIATION_TTL_MINUTES));
    }

    // 카운터 오퍼 최대 도달 시 즉시 FINAL_ROUND로 전환돼서 이구동성 게임 하듯
    public void makeOffer(Long memberId, Long price, Clock clock) {
        validateNegotiating();
        if (this.lastOfferedBy.equals(memberId)) {
            throw new BusinessException(NegotiationCode.NOT_YOUR_TURN);
        }
        this.currentPrice = price;
        this.lastOfferedBy = memberId;
        this.offerCount++;
        this.expiresAt = LocalDateTime.now(clock).plusMinutes(NEGOTIATION_TTL_MINUTES);

        if (this.offerCount >= MAX_OFFERS) {
            this.status = NegotiationStatus.FINAL_ROUND;
        }
    }

    public void accept(Long memberId) {
        validateNegotiating();
        if (this.lastOfferedBy.equals(memberId)) {
            throw new BusinessException(NegotiationCode.SELF_ACCEPT);
        }
        this.status = NegotiationStatus.ACCEPTED;
    }

    public void reject() {
        validateNegotiating();
        this.status = NegotiationStatus.REJECTED;
    }

    /**
     * 봉인 입찰 최종가 이구동성 게임
     * 양쪽 모두 제출 완료 시: buyerFinalPrice >= sellerFinalPrice면 자동 거래 (sellerFinalPrice로 체결).
     *
     * @return true = 거래 성사, false = 대기 중 또는 불일치
     */
    public boolean submitFinalOffer(Long memberId, Long price) {
        if (this.status != NegotiationStatus.FINAL_ROUND) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
        if (!isParticipant(memberId)) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_NOT_PARTICIPANT);
        }

        if (sellerId.equals(memberId)) {
            if (this.sellerFinalPrice != null) {
                throw new BusinessException(NegotiationCode.ALREADY_SUBMITTED_FINAL_OFFER);
            }
            this.sellerFinalPrice = price;
        } else {
            if (this.buyerFinalPrice != null) {
                throw new BusinessException(NegotiationCode.ALREADY_SUBMITTED_FINAL_OFFER);
            }
            this.buyerFinalPrice = price;
        }

        if (this.sellerFinalPrice != null && this.buyerFinalPrice != null) {
            if (this.buyerFinalPrice >= this.sellerFinalPrice) {
                this.currentPrice = this.sellerFinalPrice;
                this.status = NegotiationStatus.ACCEPTED;
                return true;
            } else {
                expire();
                return false;
            }
        }

        return false;
    }

    public void cancel() {
        if (this.status != NegotiationStatus.NEGOTIATING && this.status != NegotiationStatus.FINAL_ROUND) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
        this.status = NegotiationStatus.CANCELLED;
    }

    public void expire() {
        if (this.status != NegotiationStatus.NEGOTIATING && this.status != NegotiationStatus.FINAL_ROUND) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
        this.status = NegotiationStatus.EXPIRED;
    }

    public boolean isParticipant(Long memberId) {
        return this.sellerId.equals(memberId) || this.buyerId.equals(memberId);
    }

    private void validateNegotiating() {
        if (this.status != NegotiationStatus.NEGOTIATING) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_INVALID_STATE);
        }
    }
}
