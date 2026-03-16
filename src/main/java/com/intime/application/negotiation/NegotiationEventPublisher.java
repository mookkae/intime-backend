package com.intime.application.negotiation;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NegotiationEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long negotiationId, NegotiationEventDto event) {
        messagingTemplate.convertAndSend("/topic/negotiation/" + negotiationId, event);
    }
}
