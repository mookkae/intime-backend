package com.intime.domain.trade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradePostRepository extends JpaRepository<TradePost, Long> {
    boolean existsByWaitingTicketIdAndStatus(Long waitingTicketId, TradePostStatus status);

    // 매장 별 거래 목록 조회
    List<TradePost> findByStoreIdAndStatus(Long storeId, TradePostStatus status);
    
    Optional<TradePost> findByWaitingTicketIdAndStatus(Long waitingTicketId, TradePostStatus status);
}
