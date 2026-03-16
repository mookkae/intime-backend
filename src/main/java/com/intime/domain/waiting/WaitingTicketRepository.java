package com.intime.domain.waiting;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingTicketRepository extends JpaRepository<WaitingTicket, Long> {

    // 새 번호 발급
    Optional<WaitingTicket> findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(Long storeId, LocalDate waitingDate);

    // 다음 호출 로직
    Optional<WaitingTicket> findTopByStoreIdAndStatusOrderByPositionNumberAsc(Long storeId, WaitingStatus status);

    // 웨이팅 조회 손님용
    List<WaitingTicket> findByStoreIdAndStatusInOrderByPositionNumberAsc(Long storeId, List<WaitingStatus> statuses);

    // 특정 손님 조회
    List<WaitingTicket> findByMemberIdAndStatusIn(Long memberId, List<WaitingStatus> statuses);

    // 예상 대기 순서 팀 조회
    int countByStoreIdAndStatusAndPositionNumberLessThan(Long storeId, WaitingStatus status, int positionNumber);

    // 노쇼 처리
    List<WaitingTicket> findByStatusAndCalledAtBefore(WaitingStatus status, LocalDateTime threshold);

    // 교환한다고 기다려주거나 미루기 시
    List<WaitingTicket> findByPendingCallAtBeforeAndStatus(LocalDateTime threshold, WaitingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wt FROM WaitingTicket wt WHERE wt.id IN :ids ORDER BY wt.id ASC")
    List<WaitingTicket> findByIdsWithLock(@Param("ids") List<Long> ids);
}
