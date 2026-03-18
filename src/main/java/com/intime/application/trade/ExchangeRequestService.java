package com.intime.application.trade;

import com.intime.application.trade.dto.ExchangeRequestCommand;
import com.intime.application.trade.dto.ExchangeRequestInfo;

import java.util.List;

public interface ExchangeRequestService {

    ExchangeRequestInfo requestExchange(ExchangeRequestCommand command);

    void cancelRequest(Long requestId, Long buyerId);

    void selectBuyerAndStartNegotiation(Long requestId, Long sellerId);

    List<ExchangeRequestInfo> getPostRequests(Long postId);

    List<ExchangeRequestInfo> getMyRequests(Long buyerId);
}
