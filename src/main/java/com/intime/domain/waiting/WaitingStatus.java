package com.intime.domain.waiting;

public enum WaitingStatus {
    WAITING,
    CALLED,
    SEATED,
    CANCELLED,
    NO_SHOW;

    public boolean isCancellable() {
        return this == WAITING || this == CALLED;
    }

    public boolean isTradeable() {
        return this == WAITING || this == CALLED;
    }
}
