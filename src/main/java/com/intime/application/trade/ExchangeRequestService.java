package com.intime.application.trade;

import com.intime.domain.trade.ExchangeRequest;

import java.util.List;

public interface ExchangeRequestService {
    ExchangeRequest requestExchange(Long postId, Long buyerTicketId, Long buyerId);

    void cancelRequest(Long requestId, Long buyerId);

    void selectBuyer(Long requestId, Long sellerId);

    List<ExchangeRequest> getPostRequests(Long postId);
}
