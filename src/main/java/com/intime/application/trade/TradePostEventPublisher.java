package com.intime.application.trade;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradePostEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishPostCancelled(Long postId) {
        messagingTemplate.convertAndSend("/topic/trade-posts/" + postId,
                new TradePostEventDto(TradePostEventDto.EventType.POST_CANCELLED, postId, null));
    }

    public void publishNewRequest(Long postId, Long offerPrice) {
        messagingTemplate.convertAndSend("/topic/trade-posts/" + postId,
                new TradePostEventDto(TradePostEventDto.EventType.NEW_REQUEST, postId, offerPrice));
    }

    public record TradePostEventDto(EventType type, Long postId, Long offerPrice) {
        public enum EventType {
            POST_CANCELLED, NEW_REQUEST
        }
    }
}
