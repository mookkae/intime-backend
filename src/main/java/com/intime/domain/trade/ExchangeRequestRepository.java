package com.intime.domain.trade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findByTradePostId(Long tradePostId);

    List<ExchangeRequest> findByStatusAndExpiresAtBefore(ExchangeRequestStatus status, LocalDateTime now);

    @Modifying
    @Query("UPDATE ExchangeRequest er SET er.status = :rejected WHERE er.tradePostId = :tradePostId AND er.status = :pending AND er.id <> :excludeId")
    void rejectOtherPendingRequests(
            @Param("tradePostId") Long tradePostId,
            @Param("excludeId") Long excludeId,
            @Param("pending") ExchangeRequestStatus pending,
            @Param("rejected") ExchangeRequestStatus rejected
    );
}
