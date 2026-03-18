package com.intime.application.negotiation;

import com.intime.application.negotiation.dto.NegotiationFinalOfferCommand;
import com.intime.application.negotiation.dto.NegotiationInfo;
import com.intime.application.negotiation.dto.NegotiationOfferCommand;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
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
    public NegotiationInfo getNegotiation(Long negotiationId) {
        return NegotiationInfo.from(getNegotiationOrThrow(negotiationId));
    }

    @Override
    public NegotiationInfo getNegotiationByExchangeRequestId(Long exchangeRequestId) {
        Negotiation negotiation = negotiationRepository.findByExchangeRequestId(exchangeRequestId)
                .orElseThrow(() -> new BusinessException(NegotiationCode.NEGOTIATION_NOT_FOUND));
        return NegotiationInfo.from(negotiation);
    }

    @Override
    @Transactional
    public NegotiationInfo makeOffer(NegotiationOfferCommand command) {
        Negotiation negotiation = getNegotiationOrThrow(command.negotiationId());
        negotiation.makeOffer(command.memberId(), command.price());
        log.info("가격 제안 - negotiationId: {}, memberId: {}, price: {}, offerCount: {}, status: {}",
                command.negotiationId(), command.memberId(), command.price(),
                negotiation.getOfferCount(), negotiation.getStatus());

        if (negotiation.getStatus() == NegotiationStatus.FINAL_ROUND) {
            eventPublisher.publish(command.negotiationId(),
                    NegotiationEventDto.ofFinalRound(negotiation.getExpiresAt(), negotiation.getLastOfferedBy()));
        } else {
            eventPublisher.publish(command.negotiationId(),
                    NegotiationEventDto.ofOffer(command.price(), negotiation.getOfferCount(),
                            negotiation.getExpiresAt(), negotiation.getLastOfferedBy()));
        }

        return NegotiationInfo.from(negotiation);
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
        log.info("협상 수락 - negotiationId: {}, memberId: {}", negotiationId, memberId);
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
        log.info("협상 거절 - negotiationId: {}, memberId: {}", negotiationId, memberId);
        tradeLifecycleService.cancelRequestAndReopenPost(negotiation.getExchangeRequestId());

        eventPublisher.publish(negotiationId, NegotiationEventDto.ofRejected());
    }

    @Override
    @Transactional
    public void submitFinalOffer(NegotiationFinalOfferCommand command) {
        Negotiation negotiation = getNegotiationOrThrow(command.negotiationId());

        WaitingTicket sellerTicket = getTicket(negotiation.getSellerTicketId());
        if (!sellerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }

        WaitingTicket buyerTicket = getTicket(negotiation.getBuyerTicketId());
        if (!buyerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.BUYER_TICKET_NOT_TRADEABLE);
        }

        boolean dealReached = negotiation.submitFinalOffer(command.memberId(), command.price());
        log.info("최종가 제출 - negotiationId: {}, memberId: {}, price: {}, dealReached: {}",
                command.negotiationId(), command.memberId(), command.price(), dealReached);

        if (dealReached) {
            executeDeal(negotiation, sellerTicket);
        } else if (negotiation.getStatus() == NegotiationStatus.FAILED) {
            log.info("최종 입찰 가격 불일치 → 협상 불성사 - negotiationId: {}", command.negotiationId());
            eventPublisher.publish(command.negotiationId(), NegotiationEventDto.ofFailed());
        } else {
            eventPublisher.publish(command.negotiationId(), NegotiationEventDto.ofFinalOfferSubmitted());
        }
    }

    private void executeDeal(Negotiation negotiation, WaitingTicket sellerTicket) {
        log.info("거래 체결 시작 - negotiationId: {}, agreedPrice: {}", negotiation.getId(), negotiation.getCurrentPrice());
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

        log.info("거래 체결 완료 - negotiationId: {}", negotiation.getId());
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
