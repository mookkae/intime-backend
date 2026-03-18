package com.intime.application.waiting;

import com.intime.application.waiting.dto.WaitingPositionInfo;
import com.intime.application.waiting.dto.WaitingTicketInfo;
import com.intime.application.waiting.dto.WaitingTicketInfo.TradePostSummary;
import com.intime.common.exception.BusinessException;
import com.intime.domain.store.StoreRepository;
import com.intime.domain.trade.TradePost;
import com.intime.domain.trade.TradePostRepository;
import com.intime.domain.trade.TradePostStatus;
import com.intime.domain.waiting.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intime.domain.store.StoreCode.STORE_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingQueryServiceImpl implements WaitingQueryService {

    private final WaitingTicketRepository waitingTicketRepository;
    private final StoreRepository storeRepository;
    private final TradePostRepository tradePostRepository;

    @Override
    public List<WaitingTicketInfo> getStoreQueue(Long storeId) {
        List<WaitingTicket> tickets = waitingTicketRepository.findByStoreIdAndStatusInOrderByPositionNumberAsc(
                storeId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));

        Map<Long, TradePostSummary> tradePostByTicketId =
                tradePostRepository.findByStoreIdAndStatus(storeId, TradePostStatus.OPEN).stream()
                        .collect(Collectors.toMap(
                                TradePost::getWaitingTicketId,
                                tp -> new TradePostSummary(tp.getId(), tp.getPrice())
                        ));

        return tickets.stream()
                .map(ticket -> WaitingTicketInfo.from(ticket, tradePostByTicketId.get(ticket.getId())))
                .toList();
    }

    @Override
    public List<WaitingTicketInfo> getMyTickets(Long memberId) {
        List<WaitingTicket> tickets = waitingTicketRepository.findByMemberIdAndStatusIn(
                memberId, List.of(WaitingStatus.WAITING, WaitingStatus.CALLED));

        List<Long> ticketIds = tickets.stream().map(WaitingTicket::getId).toList();
        Map<Long, TradePostSummary> tradePostByTicketId = ticketIds.isEmpty()
                ? Map.of()
                : tradePostRepository.findByWaitingTicketIdInAndStatus(ticketIds, TradePostStatus.OPEN).stream()
                        .collect(Collectors.toMap(
                                TradePost::getWaitingTicketId,
                                tp -> new TradePostSummary(tp.getId(), tp.getPrice())
                        ));

        return tickets.stream()
                .map(ticket -> WaitingTicketInfo.from(ticket, tradePostByTicketId.get(ticket.getId())))
                .toList();
    }

    @Override
    public WaitingPositionInfo getMyPosition(Long ticketId) {
        WaitingTicket ticket = waitingTicketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        if (ticket.getStatus() != WaitingStatus.WAITING) {
            throw new BusinessException(WaitingCode.WAITING_INVALID_STATE);
        }

        int aheadCount = waitingTicketRepository.countByStoreIdAndStatusAndPositionNumberLessThan(
                ticket.getStoreId(), WaitingStatus.WAITING, ticket.getPositionNumber());

        int estimatedWaitMinutes = storeRepository.findById(ticket.getStoreId())
                .orElseThrow(() -> new BusinessException(STORE_NOT_FOUND))
                .getEstimatedWaitMinutes() * aheadCount;

        return new WaitingPositionInfo(
                ticket.getId(),
                ticket.getPositionNumber(),
                aheadCount,
                estimatedWaitMinutes
        );
    }
}
