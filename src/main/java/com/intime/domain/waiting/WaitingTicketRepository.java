package com.intime.domain.waiting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingTicketRepository extends JpaRepository<WaitingTicket, Long> {

    Optional<WaitingTicket> findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(Long storeId, LocalDate waitingDate);

    Optional<WaitingTicket> findTopByStoreIdAndStatusOrderByPositionNumberAsc(Long storeId, WaitingStatus status);

    List<WaitingTicket> findByStoreIdAndStatusOrderByPositionNumberAsc(Long storeId, WaitingStatus status);

    List<WaitingTicket> findByMemberIdAndStatusIn(Long memberId, List<WaitingStatus> statuses);

    int countByStoreIdAndStatusAndPositionNumberLessThan(Long storeId, WaitingStatus status, int positionNumber);
}
