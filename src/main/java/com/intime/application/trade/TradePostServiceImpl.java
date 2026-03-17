package com.intime.application.trade;

import com.intime.common.exception.BusinessException;
import com.intime.domain.trade.*;
import com.intime.domain.waiting.WaitingStatus;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import com.intime.domain.waiting.WaitingCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TradePostServiceImpl implements TradePostService {

    private final TradePostRepository tradePostRepository;
    private final WaitingTicketRepository waitingTicketRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final TradePostEventPublisher tradePostEventPublisher;

    @Override
    @Transactional
    public TradePost register(Long ticketId, Long sellerId, Long price) {
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

        TradePost post = TradePost.create(ticketId, sellerId, ticket.getStoreId(), price);
        TradePost saved = tradePostRepository.save(post);
        log.info("판매 게시글 등록 - ticketId: {}, sellerId: {}, price: {}", ticketId, sellerId, price);
        return saved;
    }

    @Override
    @Transactional
    public void withdraw(Long postId, Long sellerId) {
        TradePost post = getPost(postId);
        if (!post.isOwnedBy(sellerId)) {
            throw new BusinessException(TradePostCode.TRADE_POST_NOT_OWNER);
        }
        if (post.getStatus() != TradePostStatus.OPEN) {
            throw new BusinessException(TradePostCode.TRADE_POST_INVALID_STATE);
        }
        post.cancel();
        tradePostRepository.saveAndFlush(post); // @Modifying clearAutomatically 이전에 flush 보장
        exchangeRequestRepository.cancelAllPendingByTradePostId(
                postId, ExchangeRequestStatus.PENDING, ExchangeRequestStatus.CANCELLED);
        tradePostEventPublisher.publishPostCancelled(postId);
        log.info("판매 게시글 철회 - postId: {}, sellerId: {}", postId, sellerId);
    }

    @Override
    public List<TradePost> getStoreTradePosts(Long storeId) {
        return tradePostRepository.findByStoreIdAndStatus(storeId, TradePostStatus.OPEN);
    }

    @Override
    public List<TradePost> getMyTradePosts(Long sellerId) {
        return tradePostRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }

    @Override
    public List<TradePost> getOpenPostsByTicketIds(List<Long> ticketIds) {
        if (ticketIds.isEmpty()) {
            return List.of();
        }
        return tradePostRepository.findByWaitingTicketIdInAndStatus(ticketIds, TradePostStatus.OPEN);
    }

    private TradePost getPost(Long postId) {
        return tradePostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(TradePostCode.TRADE_POST_NOT_FOUND));
    }
}
