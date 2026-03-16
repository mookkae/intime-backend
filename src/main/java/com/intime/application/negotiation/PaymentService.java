package com.intime.application.negotiation;

import com.intime.domain.negotiation.Deal;

public interface PaymentService {

    // TODO: 플랫폼 재화 차감 처리
    void processPayment(Deal deal);
}
