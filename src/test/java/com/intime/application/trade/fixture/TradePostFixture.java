package com.intime.application.trade.fixture;

import com.intime.domain.trade.TradePost;
import com.intime.support.TestReflectionUtils;

public class TradePostFixture {

    public static TradePost createPost() {
        return createPost(1L, 1L, 1L, 1L);
    }

    public static TradePost createPost(Long postId, Long ticketId, Long sellerId, Long storeId) {
        TradePost post = TradePost.create(ticketId, sellerId, storeId, null);
        if (postId != null) {
            TestReflectionUtils.setField(post, "id", postId);
        }
        return post;
    }
}
