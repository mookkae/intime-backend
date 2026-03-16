package com.intime.domain.trade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    // 교환 신청 목록
    List<ExchangeRequest> findByTradePostId(Long tradePostId);

    // 교환 요청 만료
    List<ExchangeRequest> findByStatusAndExpiresAtBefore(ExchangeRequestStatus status, LocalDateTime now);

    // 일괄 반영
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ExchangeRequest er SET er.status = :rejected WHERE er.tradePostId = :tradePostId AND er.status = :pending AND er.id <> :excludeId")
    void rejectOtherPendingRequests(
            @Param("tradePostId") Long tradePostId,
            @Param("excludeId") Long excludeId,
            @Param("pending") ExchangeRequestStatus pending,
            @Param("rejected") ExchangeRequestStatus rejected
    );
}
