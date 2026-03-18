package com.intime.application.trade;

import com.intime.application.trade.dto.ExchangeRequestCommand;
import com.intime.application.trade.dto.ExchangeRequestInfo;
import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRequestServiceImpl implements ExchangeRequestService {

    private static final int REQUEST_TTL_MINUTES = 5;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final TradePostRepository tradePostRepository;
    private final WaitingTicketRepository waitingTicketRepository;
    private final NegotiationRepository negotiationRepository;
    private final TradePostEventPublisher tradePostEventPublisher;
    private final Clock clock;

    @Override
    @Transactional
    public ExchangeRequestInfo requestExchange(ExchangeRequestCommand command) {
        TradePost post = getPost(command.postId());

        if (post.getStatus() != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }

        if (post.isOwnedBy(command.buyerId())) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_SELF_POST);
        }

        boolean isDuplicate = exchangeRequestRepository.existsByTradePostIdAndBuyerIdAndStatusIn(
                command.postId(), command.buyerId(),
                List.of(ExchangeRequestStatus.PENDING, ExchangeRequestStatus.SELECTED));
        if (isDuplicate) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_DUPLICATE);
        }

        WaitingTicket buyerTicket = waitingTicketRepository.findById(command.buyerTicketId())
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        if (!buyerTicket.isOwnedBy(command.buyerId())) {
            throw new BusinessException(ExchangeRequestCode.BUYER_TICKET_NOT_OWNER);
        }

        if (buyerTicket.getStatus() != WaitingStatus.WAITING) {
            throw new BusinessException(ExchangeRequestCode.BUYER_TICKET_NOT_WAITING);
        }

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(REQUEST_TTL_MINUTES);
        ExchangeRequest request = ExchangeRequest.create(
                command.postId(), command.buyerTicketId(), command.buyerId(), command.offerPrice(), expiresAt);
        ExchangeRequest saved = exchangeRequestRepository.save(request);
        log.info("교환 신청 완료 - postId: {}, buyerId: {}, buyerTicketId: {}, offerPrice: {}",
                command.postId(), command.buyerId(), command.buyerTicketId(), command.offerPrice());
        tradePostEventPublisher.publishNewRequest(command.postId(), command.offerPrice());
        return ExchangeRequestInfo.from(saved);
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId, Long buyerId) {
        ExchangeRequest request = getRequest(requestId);
        if (!request.isOwnedBy(buyerId)) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_OWNER);
        }
        request.cancel();
        log.info("교환 신청 취소 - requestId: {}, buyerId: {}", requestId, buyerId);
    }

    @Override
    @Transactional
    public void selectBuyerAndStartNegotiation(Long requestId, Long sellerId) {
        ExchangeRequest selectedRequest = getRequest(requestId);
        TradePost post = getPost(selectedRequest.getTradePostId());

        if (!post.isOwnedBy(sellerId)) {
            throw new BusinessException(TradePostCode.TRADE_POST_NOT_OWNER);
        }

        if (post.getStatus() != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }

        selectedRequest.select();
        post.startNegotiation();
        
        Negotiation negotiation = Negotiation.create(
                selectedRequest.getId(),
                sellerId,
                selectedRequest.getBuyerId(),
                post.getWaitingTicketId(),
                selectedRequest.getBuyerTicketId(),
                selectedRequest.getOfferPrice(),
                clock
        );
        negotiationRepository.save(negotiation);
        log.info("구매자 선택 + 협상 시작 - requestId: {}, sellerId: {}, buyerId: {}",
                requestId, sellerId, selectedRequest.getBuyerId());
    }

    @Override
    public List<ExchangeRequestInfo> getPostRequests(Long postId) {
        return exchangeRequestRepository.findByTradePostId(postId).stream()
                .map(ExchangeRequestInfo::from)
                .toList();
    }

    @Override
    public List<ExchangeRequestInfo> getMyRequests(Long buyerId) {
        return exchangeRequestRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId).stream()
                .map(ExchangeRequestInfo::from)
                .toList();
    }

    private TradePost getPost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(TradePostCode.TRADE_POST_NOT_FOUND));
    }

    private ExchangeRequest getRequest(Long requestId) {
        return exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_FOUND));
    }
}
