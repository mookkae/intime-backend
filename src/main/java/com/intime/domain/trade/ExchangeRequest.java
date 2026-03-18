package com.intime.domain.trade;

import com.intime.common.BaseTimeEntity;
import com.intime.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "exchange_request",
        indexes = {
                @Index(name = "idx_exchange_request_trade_post_status", columnList = "trade_post_id, status"),
                @Index(name = "idx_exchange_request_buyer_ticket_status", columnList = "buyer_ticket_id, status"),
                @Index(name = "idx_exchange_request_buyer_id", columnList = "buyer_id"),
                @Index(name = "idx_exchange_request_status_expires", columnList = "status, expires_at")
        }
)
public class ExchangeRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Long tradePostId;

    @Column(nullable = false)
    private Long buyerTicketId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long offerPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private ExchangeRequest(Long tradePostId, Long buyerTicketId, Long buyerId,
                            Long offerPrice, LocalDateTime expiresAt) {
        this.tradePostId = tradePostId;
        this.buyerTicketId = buyerTicketId;
        this.buyerId = buyerId;
        this.offerPrice = offerPrice;
        this.status = ExchangeRequestStatus.PENDING;
        this.expiresAt = expiresAt;
    }

    public static ExchangeRequest create(Long tradePostId, Long buyerTicketId, Long buyerId,
                                         Long offerPrice, LocalDateTime expiresAt) {
        return new ExchangeRequest(tradePostId, buyerTicketId, buyerId, offerPrice, expiresAt);
    }

    public void select() {
        validatePending();
        this.status = ExchangeRequestStatus.SELECTED;
    }

    public void complete() {
        validateSelected();
        this.status = ExchangeRequestStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status != ExchangeRequestStatus.PENDING &&
                this.status != ExchangeRequestStatus.SELECTED) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
        this.status = ExchangeRequestStatus.CANCELLED;
    }

    public void expire() {
        validatePending();
        this.status = ExchangeRequestStatus.EXPIRED;
    }

    public boolean isOwnedBy(Long buyerId) {
        return this.buyerId.equals(buyerId);
    }

    private void validatePending() {
        if (this.status != ExchangeRequestStatus.PENDING) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }

    private void validateSelected() {
        if (this.status != ExchangeRequestStatus.SELECTED) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_INVALID_STATE);
        }
    }
}
