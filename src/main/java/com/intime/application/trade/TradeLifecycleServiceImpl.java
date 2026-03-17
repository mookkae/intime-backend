package com.intime.application.trade;

import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.NegotiationRepository;
import com.intime.domain.negotiation.NegotiationStatus;
import com.intime.domain.trade.ExchangeRequest;
import com.intime.domain.trade.ExchangeRequestCode;
import com.intime.domain.trade.ExchangeRequestRepository;
import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostCode;
import com.intime.domain.trade.TradePostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradeLifecycleServiceImpl implements TradeLifecycleService {

    private final NegotiationRepository negotiationRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final TradePostRepository tradePostRepository;

    @Override
    @Transactional
    public void cancelActiveNegotiationByTicket(Long ticketId) {
        negotiationRepository.findBySellerTicketIdAndStatusIn(ticketId, NegotiationStatus.ACTIVE_STATUSES)
                .ifPresent(negotiation -> {
                    log.warn("협상 강제 종료 (순번 호출) - ticketId: {}, negotiationId: {}", ticketId, negotiation.getId());
                    negotiation.cancel();

                    ExchangeRequest request = getRequest(negotiation.getExchangeRequestId());
                    request.cancel();

                    TradePost post = getPost(request.getTradePostId());
                    post.cancel();
                });
    }

    @Override
    @Transactional
    public void cancelRequestAndReopenPost(Long exchangeRequestId) {
        ExchangeRequest request = getRequest(exchangeRequestId);
        request.cancel();

        TradePost post = getPost(request.getTradePostId());
        post.reopen();
        log.info("신청 취소 + 포스트 재오픈 - exchangeRequestId: {}, postId: {}", exchangeRequestId, post.getId());
    }

    private ExchangeRequest getRequest(Long requestId) {
        return exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ExchangeRequestCode.EXCHANGE_REQUEST_NOT_FOUND));
    }

    private TradePost getPost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(TradePostCode.TRADE_POST_NOT_FOUND));
    }
}
