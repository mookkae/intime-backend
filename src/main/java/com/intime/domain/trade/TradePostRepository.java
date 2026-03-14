package com.intime.domain.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradePostRepository extends JpaRepository<TradePost, Long> {
    boolean existsByWaitingTicketIdAndStatus(Long waitingTicketId, TradePostStatus status);

    List<TradePost> findByStoreIdAndStatus(Long storeId, TradePostStatus status);
}
