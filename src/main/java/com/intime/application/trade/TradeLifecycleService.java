package com.intime.application.trade;

public interface TradeLifecycleService {

    // 협상이 잘 돼서 나머지 들이 cancel → 교환신청 cancel → 게시글 cancel
    void cancelActiveNegotiationByTicket(Long ticketId);

    // 협상 실패(거절/만료) 후 교환신청을 취소하고 게시글을 재오픈
    void cancelRequestAndReopenPost(Long exchangeRequestId);
}
