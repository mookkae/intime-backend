package com.intime.application.waiting;

import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostRepository;
import com.intime.domain.trade.TradePostStatus;
import com.intime.domain.waiting.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingServiceImpl implements WaitingService {

    private final WaitingTicketRepository waitingTicketRepository;
    private final TradePostRepository tradePostRepository;
    private final NegotiationRepository negotiationRepository;
    private final WaitingEventPublisher waitingEventPublisher;
    private final Clock clock;

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public WaitingTicket register(Long storeId, Long memberId, int partySize) {
        LocalDate today = LocalDate.now(clock);

        int nextPosition = waitingTicketRepository
                .findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(storeId, today)
                .map(ticket -> ticket.getPositionNumber() + 1)
                .orElse(1);

        WaitingTicket ticket = WaitingTicket.create(storeId, memberId, nextPosition, partySize, today);

        try {
            return waitingTicketRepository.save(ticket);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(WaitingCode.WAITING_REGISTER_FAILED);
        }
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void cancel(Long ticketId, Long memberId) {
        WaitingTicket ticket = getTicket(ticketId);
        if (!ticket.isOwnedBy(memberId)) {
            throw new BusinessException(WaitingCode.WAITING_NOT_OWNER);
        }
        ticket.cancel();
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public WaitingTicket callNext(Long storeId) {
        WaitingTicket ticket = waitingTicketRepository
                .findTopByStoreIdAndStatusOrderByPositionNumberAsc(storeId, WaitingStatus.WAITING)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NO_ONE_WAITING));

        LocalDateTime now = LocalDateTime.now(clock);

        tradePostRepository.findByWaitingTicketIdAndStatus(ticket.getId(), TradePostStatus.OPEN)
                .ifPresent(TradePost::cancel);

        boolean hasNegotiating = negotiationRepository
                .findBySellerTicketIdAndStatusIn(ticket.getId(), NegotiationStatus.ACTIVE_STATUSES)
                .isPresent();

        if (hasNegotiating) {
            ticket.markPendingCall(now);
            waitingEventPublisher.publishPendingCall(ticket.getId());
        } else {
            ticket.call(now);
            waitingEventPublisher.publishCalled(ticket.getId());
        }

        return ticket;
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void confirmSeated(Long ticketId) {
        getTicket(ticketId).seat();
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void markNoShow(Long ticketId) {
        getTicket(ticketId).noShow();
    }

    private WaitingTicket getTicket(Long ticketId) {
        return waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));
    }
}
