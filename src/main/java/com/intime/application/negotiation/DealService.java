package com.intime.application.negotiation;

import com.intime.domain.negotiation.Deal;
import com.intime.domain.negotiation.Negotiation;

public interface DealService {

    Deal executeTrade(Negotiation negotiation);
}
