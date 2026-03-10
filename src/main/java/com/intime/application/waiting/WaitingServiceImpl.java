package com.intime.application.waiting;

import com.intime.common.exception.BusinessException;
import com.intime.domain.waiting.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingServiceImpl implements WaitingService {

    private final WaitingTicketRepository waitingTicketRepository;

    @Override
    @Transactional
    public WaitingTicket register(Long storeId, Long memberId, int partySize) {
        int nextPosition = waitingTicketRepository
                .findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(storeId, LocalDate.now())
                .map(ticket -> ticket.getPositionNumber() + 1)
                .orElse(1);

        WaitingTicket ticket = WaitingTicket.create(storeId, memberId, nextPosition, partySize, LocalDate.now());
        return waitingTicketRepository.save(ticket);
    }

    @Override
    @Transactional
    public void cancel(Long ticketId, Long memberId) {
        WaitingTicket ticket = getTicket(ticketId);
        if (!ticket.isOwnedBy(memberId)) {
            throw new BusinessException(WaitingCode.WAITING_NOT_OWNER);
        }
        ticket.cancel();
    }

    @Override
    @Transactional
    public WaitingTicket callNext(Long storeId) {
        WaitingTicket ticket = waitingTicketRepository
                .findTopByStoreIdAndStatusOrderByPositionNumberAsc(storeId, WaitingStatus.WAITING)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NO_ONE_WAITING));
        ticket.call();
        return ticket;
    }

    @Override
    @Transactional
    public void confirmSeated(Long ticketId) {
        getTicket(ticketId).seat();
    }

    @Override
    @Transactional
    public void markNoShow(Long ticketId) {
        getTicket(ticketId).noShow();
    }

    private WaitingTicket getTicket(Long ticketId) {
        return waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));
    }
}
