package com.intime.application.waiting;

import com.intime.application.trade.TradePostEventPublisher;
import com.intime.application.waiting.dto.WaitingRegisterCommand;
import com.intime.application.waiting.dto.WaitingTicketInfo;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingServiceImpl implements WaitingService {

    private final WaitingTicketRepository waitingTicketRepository;
    private final TradePostRepository tradePostRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final NegotiationRepository negotiationRepository;
    private final WaitingEventPublisher waitingEventPublisher;
    private final TradePostEventPublisher tradePostEventPublisher;
    private final Clock clock;

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public WaitingTicketInfo register(WaitingRegisterCommand command) {
        LocalDate today = LocalDate.now(clock);

        boolean isDuplicate = waitingTicketRepository.existsByMemberIdAndStoreIdAndWaitingDateAndStatusIn(
                command.memberId(), command.storeId(), today, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));
        if (isDuplicate) {
            throw new BusinessException(WaitingCode.WAITING_DUPLICATE);
        }

        int nextPosition = waitingTicketRepository
                .findTopByStoreIdAndWaitingDateOrderByPositionNumberDesc(command.storeId(), today)
                .map(ticket -> ticket.getPositionNumber() + 1)
                .orElse(1);

        WaitingTicket ticket = WaitingTicket.create(
                command.storeId(), command.memberId(), nextPosition, command.partySize(), today);

        try {
            WaitingTicket saved = waitingTicketRepository.save(ticket);
            log.info("웨이팅 등록 완료 - storeId: {}, memberId: {}, positionNumber: {}",
                    command.storeId(), command.memberId(), nextPosition);
            return WaitingTicketInfo.from(saved);
        } catch (DataIntegrityViolationException e) {
            log.warn("웨이팅 등록 실패 (순번 충돌) - storeId: {}, memberId: {}",
                    command.storeId(), command.memberId());
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

        waitingTicketRepository.saveAndFlush(ticket);
        log.info("웨이팅 취소 - ticketId: {}, memberId: {}", ticketId, memberId);

        // 판매자로서 게시한 교환 게시글 취소 → 구매자 신청 일괄 취소 + 알림
        tradePostRepository.findByWaitingTicketIdAndStatus(ticket.getId(), TradePostStatus.OPEN)
                .ifPresent(post -> {
                    post.cancel();
                    tradePostRepository.saveAndFlush(post);
                    exchangeRequestRepository.cancelAllPendingByTradePostId(
                            post.getId(), ExchangeRequestStatus.PENDING, ExchangeRequestStatus.CANCELLED);
                    tradePostEventPublisher.publishPostCancelled(post.getId());
                    log.info("웨이팅 취소로 인한 판매 게시글 취소 - postId: {}", post.getId());
                });

        int cancelledCount = exchangeRequestRepository.cancelAllPendingByBuyerTicketId(
                ticketId, ExchangeRequestStatus.PENDING, ExchangeRequestStatus.CANCELLED);
        if (cancelledCount > 0) {
            log.info("웨이팅 취소로 인한 교환 신청 취소 - buyerTicketId: {}, count: {}", ticketId, cancelledCount);
        }
    }

    @Override
    @Transactional
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 100))
    public WaitingTicketInfo callNext(Long storeId) {
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
            log.info("순번 호출 유예 (협상 중) - storeId: {}, ticketId: {}", storeId, ticket.getId());
        } else {
            ticket.call(now);
            waitingEventPublisher.publishCalled(ticket.getId());
            log.info("순번 호출 - storeId: {}, ticketId: {}, positionNumber: {}",
                    storeId, ticket.getId(), ticket.getPositionNumber());
        }

        return WaitingTicketInfo.from(ticket);
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
