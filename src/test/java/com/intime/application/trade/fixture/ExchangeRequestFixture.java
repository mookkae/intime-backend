package com.intime.application.trade.fixture;

import com.intime.domain.trade.ExchangeRequest;
import com.intime.support.TestReflectionUtils;

import java.time.LocalDateTime;

public class ExchangeRequestFixture {

    public static ExchangeRequest createRequest() {
        return createRequest(1L, 1L, 1L, 2L);
    }

    public static ExchangeRequest createRequest(Long requestId, Long postId, Long buyerTicketId, Long buyerId) {
        ExchangeRequest request = ExchangeRequest.create(postId, buyerTicketId, buyerId,
                LocalDateTime.of(2026, 3, 14, 12, 5, 0));
        if (requestId != null) {
            TestReflectionUtils.setField(request, "id", requestId);
        }
        return request;
    }
}
