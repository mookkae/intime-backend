package com.intime.application.trade;

import com.intime.common.exception.BusinessException;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import com.intime.domain.waiting.WaitingCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradePostServiceImpl implements TradePostService {

    private final TradePostRepository tradePostRepository;
    private final WaitingTicketRepository waitingTicketRepository;

    @Override
    @Transactional
    public TradePost register(Long ticketId, Long sellerId, String description) {
        WaitingTicket ticket = waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        if (!ticket.isOwnedBy(sellerId)) {
            throw new BusinessException(TradePostCode.TRADE_POST_NOT_OWNER);
        }

        if (ticket.getStatus() != WaitingStatus.WAITING) {
            throw new BusinessException(TradePostCode.TICKET_NOT_TRADEABLE);
        }

        if (tradePostRepository.existsByWaitingTicketIdAndStatus(ticketId, TradePostStatus.OPEN)) {
            throw new BusinessException(TradePostCode.TRADE_POST_DUPLICATE);
        }

        TradePost post = TradePost.create(ticketId, sellerId, ticket.getStoreId(), description);
        return tradePostRepository.save(post);
    }

    @Override
    @Transactional
    public void withdraw(Long postId, Long sellerId) {
        TradePost post = getPost(postId);
        if (!post.isOwnedBy(sellerId)) {
            throw new BusinessException(TradePostCode.TRADE_POST_NOT_OWNER);
        }
        post.cancel();
    }

    @Override
    public List<TradePost> getStoreTradePosts(Long storeId) {
        return tradePostRepository.findByStoreIdAndStatus(storeId, TradePostStatus.OPEN);
    }

    private TradePost getPost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(TradePostCode.TRADE_POST_NOT_FOUND));
    }
}
