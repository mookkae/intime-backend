package com.intime.domain.negotiation;

import java.util.List;

public enum NegotiationStatus {
    NEGOTIATING, FINAL_ROUND, ACCEPTED, REJECTED, EXPIRED, CANCELLED;

    public static final List<NegotiationStatus> ACTIVE_STATUSES = List.of(NEGOTIATING, FINAL_ROUND);
}
