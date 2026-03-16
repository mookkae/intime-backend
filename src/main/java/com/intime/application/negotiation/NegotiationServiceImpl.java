package com.intime.application.negotiation;

import com.intime.application.trade.TradeLifecycleService;
import com.intime.application.waiting.WaitingEventPublisher;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationCode;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestCode;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.ExchangeRequestStatus;
import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostCode;
import com.intime.domain.trade.TradePostRepository;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NegotiationServiceImpl implements NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final TradePostRepository tradePostRepository;
    private final WaitingTicketRepository waitingTicketRepository;
    private final DealService dealService;
    private final TradeLifecycleService tradeLifecycleService;
    private final NegotiationEventPublisher eventPublisher;
    private final WaitingEventPublisher waitingEventPublisher;
    private final Clock clock;

    @Override
    public Negotiation getNegotiation(Long negotiationId) {
        return getNegotiationOrThrow(negotiationId);
    }

    @Override
    @Transactional
    public Negotiation makeOffer(Long negotiationId, Long memberId, Long price) {
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);
        negotiation.makeOffer(memberId, price, clock);

        if (negotiation.getStatus() == NegotiationStatus.FINAL_ROUND) {
            eventPublisher.publish(negotiationId,
                    NegotiationEventDto.ofFinalRound(negotiation.getExpiresAt()));
        } else {
            eventPublisher.publish(negotiationId,
                    NegotiationEventDto.ofOffer(price, negotiation.getOfferCount(), negotiation.getExpiresAt()));
        }

        return negotiation;
    }

    @Override
    @Transactional
    public void accept(Long negotiationId, Long memberId) {
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        if (!negotiation.isParticipant(memberId)) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_NOT_PARTICIPANT);
        }

        WaitingTicket sellerTicket = getTicket(negotiation.getSellerTicketId());
        if (!sellerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }

        WaitingTicket buyerTicket = getTicket(negotiation.getBuyerTicketId());
        if (!buyerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.BUYER_TICKET_NOT_TRADEABLE);
        }

        negotiation.accept(memberId);
        executeDeal(negotiation, sellerTicket);
    }

    @Override
    @Transactional
    public void reject(Long negotiationId, Long memberId) {
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        if (!negotiation.isParticipant(memberId)) {
            throw new BusinessException(NegotiationCode.NEGOTIATION_NOT_PARTICIPANT);
        }

        negotiation.reject();
        tradeLifecycleService.cancelRequestAndReopenPost(negotiation.getExchangeRequestId());

        eventPublisher.publish(negotiationId, NegotiationEventDto.ofRejected());
    }

    @Override
    @Transactional
    public void submitFinalOffer(Long negotiationId, Long memberId, Long price) {
        Negotiation negotiation = getNegotiationOrThrow(negotiationId);

        WaitingTicket sellerTicket = getTicket(negotiation.getSellerTicketId());
        if (!sellerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }

        WaitingTicket buyerTicket = getTicket(negotiation.getBuyerTicketId());
        if (!buyerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.BUYER_TICKET_NOT_TRADEABLE);
        }

        boolean dealReached = negotiation.submitFinalOffer(memberId, price);

        if (dealReached) {
            executeDeal(negotiation, sellerTicket);
        } else if (negotiation.getStatus() == NegotiationStatus.EXPIRED) {
            // 양쪽 모두 제출했지만 가격 불일치 → 협상 실패
            eventPublisher.publish(negotiationId, NegotiationEventDto.ofExpired());
        } else {
            // 한쪽만 제출한 상태 → 상대방에게 제출 요청 알림
            eventPublisher.publish(negotiationId, NegotiationEventDto.ofFinalOfferSubmitted());
        }
    }

    private void executeDeal(Negotiation negotiation, WaitingTicket sellerTicket) {
        dealService.executeTrade(negotiation);

        ExchangeRequest request = getRequest(negotiation.getExchangeRequestId());
        request.complete();

        TradePost post = getPost(request.getTradePostId());
        post.close();

        exchangeRequestRepository.rejectOtherPendingRequests(
                post.getId(), request.getId(),
                ExchangeRequestStatus.PENDING, ExchangeRequestStatus.REJECTED
        );

        if (sellerTicket.hasPendingCall()) {
            sellerTicket.call(LocalDateTime.now(clock));
            sellerTicket.clearPendingCall();
            waitingEventPublisher.publishCalled(sellerTicket.getId());
        }

        eventPublisher.publish(negotiation.getId(),
                NegotiationEventDto.ofAccepted(negotiation.getCurrentPrice()));
    }

    private Negotiation getNegotiationOrThrow(Long negotiationId) {
        return negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new BusinessException(NegotiationCode.NEGOTIATION_NOT_FOUND));
    }

    private ExchangeRequest getRequest(Long requestId) {
        return exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_FOUND));
    }

    private TradePost getPost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(TradePostCode.TRADE_POST_NOT_FOUND));
    }

    private WaitingTicket getTicket(Long ticketId) {
        return waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));
    }
}
