package com.intime.domain.negotiation;

import com.intime.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "deal",
        indexes = {
                @Index(name = "idx_deal_negotiation_id", columnList = "negotiation_id")
        }
)
public class Deal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long negotiationId;

    @Column(nullable = false)
    private Long sellerTicketId;

    @Column(nullable = false)
    private Long buyerTicketId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long agreedPrice;

    @Builder
    private Deal(Long negotiationId, Long sellerTicketId, Long buyerTicketId,
                 Long sellerId, Long buyerId, Long agreedPrice) {
        this.negotiationId = negotiationId;
        this.sellerTicketId = sellerTicketId;
        this.buyerTicketId = buyerTicketId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.agreedPrice = agreedPrice;
    }

    public static Deal create(Negotiation negotiation) {
        return new Deal(
                negotiation.getId(),
                negotiation.getSellerTicketId(),
                negotiation.getBuyerTicketId(),
                negotiation.getSellerId(),
                negotiation.getBuyerId(),
                negotiation.getCurrentPrice()
        );
    }
}