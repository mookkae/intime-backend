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

    // 중복 교환 신청 체크
    boolean existsByTradePostIdAndBuyerIdAndStatusIn(Long tradePostId, Long buyerId, List<ExchangeRequestStatus> statuses);

    // 일괄 반영
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ExchangeRequest er SET er.status = :rejected WHERE er.tradePostId = :tradePostId AND er.status = :pending AND er.id <> :excludeId")
    void rejectOtherPendingRequests(
            @Param("tradePostId") Long tradePostId,
            @Param("excludeId") Long excludeId,
            @Param("pending") ExchangeRequestStatus pending,
            @Param("rejected") ExchangeRequestStatus rejected
    );

    // 게시글 취소 시 대기 신청 일괄 취소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ExchangeRequest er SET er.status = :cancelled WHERE er.tradePostId = :postId AND er.status = :pending")
    void cancelAllPendingByTradePostId(
            @Param("postId") Long postId,
            @Param("pending") ExchangeRequestStatus pending,
            @Param("cancelled") ExchangeRequestStatus cancelled
    );

    // 구매자 대기표 취소 시 해당 대기표로 신청한 교환 신청 일괄 취소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ExchangeRequest er SET er.status = :cancelled WHERE er.buyerTicketId = :buyerTicketId AND er.status = :pending")
    int cancelAllPendingByBuyerTicketId(
            @Param("buyerTicketId") Long buyerTicketId,
            @Param("pending") ExchangeRequestStatus pending,
            @Param("cancelled") ExchangeRequestStatus cancelled
    );
}
