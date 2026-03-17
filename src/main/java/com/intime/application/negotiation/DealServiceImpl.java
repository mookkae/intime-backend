package com.intime.application.negotiation;

import com.intime.common.exception.BusinessException;
import com.intime.domain.negotiation.Deal;
import com.intime.domain.negotiation.DealRepository;
import com.intime.domain.negotiation.Negotiation;
import com.intime.domain.negotiation.NegotiationCode;
import com.intime.domain.waiting.WaitingCode;
import com.intime.domain.waiting.WaitingTicket;
import com.intime.domain.waiting.WaitingTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealServiceImpl implements DealService {

    private final WaitingTicketRepository waitingTicketRepository;
    private final DealRepository dealRepository;

    @Override
    @Transactional
    public Deal executeTrade(Negotiation negotiation) {
        List<Long> ids = Stream.of(negotiation.getSellerTicketId(), negotiation.getBuyerTicketId()).sorted().toList();

        List<WaitingTicket> tickets = waitingTicketRepository.findByIdsWithLock(ids);

        WaitingTicket sellerTicket = tickets.stream()
                .filter(t -> t.getId().equals(negotiation.getSellerTicketId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE));

        WaitingTicket buyerTicket = tickets.stream()
                .filter(t -> t.getId().equals(negotiation.getBuyerTicketId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(WaitingCode.WAITING_NOT_FOUND));

        if (!sellerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.SELLER_TICKET_NOT_TRADEABLE);
        }
        if (!buyerTicket.isTradeable()) {
            throw new BusinessException(NegotiationCode.BUYER_TICKET_NOT_TRADEABLE);
        }

        sellerTicket.reassignTo(negotiation.getBuyerId());
        buyerTicket.reassignTo(negotiation.getSellerId());
        log.info("순번 소유자 교환 - sellerTicketId: {} → buyerId: {}, buyerTicketId: {} → sellerId: {}",
                sellerTicket.getId(), negotiation.getBuyerId(),
                buyerTicket.getId(), negotiation.getSellerId());

        Deal deal = Deal.create(negotiation);
        return dealRepository.save(deal);
    }
}
