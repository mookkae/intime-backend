package com.intime.application.waiting;

import com.intime.common.exception.BusinessException;
import com.intime.domain.store.StoreRepository;
import com.intime.domain.waiting.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.intime.domain.store.StoreCode.STORE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingQueryServiceImpl implements WaitingQueryService {

    private final WaitingTicketRepository waitingTicketRepository;
    private final StoreRepository storeRepository;

    @Override
    public List<WaitingTicket> getStoreQueue(Long storeId) {
        return waitingTicketRepository.findByStoreIdAndStatusOrderByPositionNumberAsc(storeId, WaitingStatus.WAITING);
    }

    @Override
    public List<WaitingTicket> getMyTickets(Long memberId) {
        return waitingTicketRepository.findByMemberIdAndStatusIn(
                memberId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
    }

    @Override
    public WaitingPositionResponse getMyPosition(Long ticketId) {
        WaitingTicket ticket = waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        int aheadCount = waitingTicketRepository.countByStoreIdAndStatusAndPositionNumberLessThan(
                ticket.getStoreId(), WaitingStatus.WAITING, ticket.getPositionNumber());

        int estimatedWaitMinutes = storeRepository.findById(ticket.getStoreId())
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND))
                .getEstimatedWaitMinutes() * aheadCount;

        return new WaitingPositionResponse(
                ticket.getId(),
                ticket.getPositionNumber(),
                aheadCount,
                estimatedWaitMinutes
        );
    }
}
