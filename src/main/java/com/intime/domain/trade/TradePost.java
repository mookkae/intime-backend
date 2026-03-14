package com.intime.domain.trade;

import com.intime.common.BaseTimeEntity;
import com.intime.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "trade_post",
        indexes = {
                @Index(name = "idx_trade_post_store_status", columnList = "store_id, status"),
                @Index(name = "idx_trade_post_ticket_status", columnList = "waiting_ticket_id, status")
        }
)
public class TradePost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long waitingTicketId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradePostStatus status;

    @Column
    private String description;

    @Builder(access = AccessLevel.PRIVATE)
    private TradePost(Long waitingTicketId, Long sellerId, Long storeId, String description) {
        this.waitingTicketId = waitingTicketId;
        this.sellerId = sellerId;
        this.storeId = storeId;
        this.description = description;
        this.status = TradePostStatus.OPEN;
    }

    public static TradePost create(Long waitingTicketId, Long sellerId, Long storeId, String description) {
        return TradePost.builder()
                .waitingTicketId(waitingTicketId)
                .sellerId(sellerId)
                .storeId(storeId)
                .description(description)
                .build();
    }

    public void close() {
        validateOpen();
        this.status = TradePostStatus.CLOSED;
    }

    public void cancel() {
        validateOpen();
        this.status = TradePostStatus.CANCELLED;
    }

    private void validateOpen() {
        if (this.status != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }
    }

    public boolean isOwnedBy(Long sellerId) {
        return this.sellerId.equals(sellerId);
    }
}
