package com.intime.application.trade;

import com.intime.common.exception.BusinessException;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRequestServiceImpl implements ExchangeRequestService {

    private static final int REQUEST_TTL_MINUTES = 5;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final TradePostRepository tradePostRepository;
    private final WaitingTicketRepository waitingTicketRepository;
    private final Clock clock;

    @Override
    @Transactional
    public ExchangeRequest requestExchange(Long postId, Long buyerTicketId, Long buyerId) {
        TradePost post = getPost(postId);

        if (post.getStatus() != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }

        if (post.isOwnedBy(buyerId)) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_SELF_POST);
        }

        WaitingTicket buyerTicket = waitingTicketRepository.findById(buyerTicketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        if (!buyerTicket.isOwnedBy(buyerId)) {
            throw new BusinessException(ExchangeRequestCode.BUYER_TICKET_NOT_OWNER);
        }

        if (buyerTicket.getStatus() != WaitingStatus.WAITING) {
            throw new BusinessException(ExchangeRequestCode.BUYER_TICKET_NOT_WAITING);
        }

        LocalDateTime expiresAt = LocalDateTime.now(clock).plusMinutes(REQUEST_TTL_MINUTES);
        ExchangeRequest request = ExchangeRequest.create(postId, buyerTicketId, buyerId, expiresAt);
        return exchangeRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void cancelRequest(Long requestId, Long buyerId) {
        ExchangeRequest request = getRequest(requestId);
        if (!request.isOwnedBy(buyerId)) {
            throw new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_OWNER);
        }
        request.cancel();
    }

    @Override
    @Transactional
    public void selectBuyer(Long requestId, Long sellerId) {
        ExchangeRequest selectedRequest = getRequest(requestId);
        TradePost post = getPost(selectedRequest.getTradePostId());

        if (!post.isOwnedBy(sellerId)) {
            throw new BusinessException(TradePostCode.TRADE_POST_NOT_OWNER);
        }

        if (post.getStatus() != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }

        selectedRequest.select();
        exchangeRequestRepository.rejectOtherPendingRequests(
                post.getId(), selectedRequest.getId(),
                ExchangeRequestStatus.PENDING, ExchangeRequestStatus.REJECTED
        );
        post.close();
    }

    @Override
    public List<ExchangeRequest> getPostRequests(Long postId) {
        return exchangeRequestRepository.findByTradePostId(postId);
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
