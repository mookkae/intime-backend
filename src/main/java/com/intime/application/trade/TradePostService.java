package com.intime.application.trade;

import com.intime.application.trade.dto.TradePostInfo;
import com.intime.application.trade.dto.TradePostRegisterCommand;

import java.util.List;

public interface TradePostService {

    TradePostInfo register(TradePostRegisterCommand command);

    void withdraw(Long postId, Long sellerId);

    List<TradePostInfo> getStoreTradePosts(Long storeId);

    List<TradePostInfo> getMyTradePosts(Long sellerId);
}
