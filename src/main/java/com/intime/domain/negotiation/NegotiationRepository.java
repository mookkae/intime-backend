package com.intime.domain.negotiation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    List<Negotiation> findByStatusInAndExpiresAtBefore(List<NegotiationStatus> statuses, LocalDateTime threshold);

    Optional<Negotiation> findBySellerTicketIdAndStatusIn(Long sellerTicketId, List<NegotiationStatus> statuses);

    Optional<Negotiation> findByExchangeRequestId(Long exchangeRequestId);
}
