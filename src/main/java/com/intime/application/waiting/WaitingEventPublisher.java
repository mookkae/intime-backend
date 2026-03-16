package com.intime.application.waiting;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishPendingCall(Long ticketId) {
        messagingTemplate.convertAndSend("/topic/waiting/" + ticketId,
                new WaitingEventDto(WaitingEventDto.EventType.PENDING_CALL, ticketId));
    }

    public void publishCalled(Long ticketId) {
        messagingTemplate.convertAndSend("/topic/waiting/" + ticketId,
                new WaitingEventDto(WaitingEventDto.EventType.CALLED, ticketId));
    }

    public record WaitingEventDto(EventType type, Long ticketId) {
        public enum EventType {
            PENDING_CALL, CALLED
        }
    }
}
