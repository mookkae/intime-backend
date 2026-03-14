package com.intime.domain.waiting;

import com.intime.common.BaseTimeEntity;
import com.intime.common.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "waiting_ticket",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "waiting_date", "position_number"}),
        indexes = {
                @Index(name = "idx_waiting_ticket_store_status", columnList = "store_id, status"),
                @Index(name = "idx_waiting_ticket_status_called_at", columnList = "status, called_at"),
                @Index(name = "idx_waiting_ticket_member_status", columnList = "member_id, status")
        }
)
public class WaitingTicket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private int positionNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitingStatus status;

    @Column(nullable = false)
    private int partySize;

    @Column(nullable = false)
    private LocalDate waitingDate;

    @Column
    private LocalDateTime calledAt;

    @Builder(access = AccessLevel.PRIVATE)
    private WaitingTicket(Long storeId, Long memberId, int positionNumber, int partySize, LocalDate waitingDate) {
        this.storeId = storeId;
        this.memberId = memberId;
        this.positionNumber = positionNumber;
        this.partySize = partySize;
        this.waitingDate = waitingDate;
        this.status = WaitingStatus.WAITING;
    }

    public static WaitingTicket create(Long storeId, Long memberId, int positionNumber, int partySize, LocalDate waitingDate) {
        return WaitingTicket.builder()
                .storeId(storeId)
                .memberId(memberId)
                .positionNumber(positionNumber)
                .partySize(partySize)
                .waitingDate(waitingDate)
                .build();
    }

    public void call(LocalDateTime calledAt) {
        validateStatus(WaitingStatus.WAITING);
        this.status = WaitingStatus.CALLED;
        this.calledAt = calledAt;
    }

    public void seat() {
        validateStatus(WaitingStatus.CALLED);
        this.status = WaitingStatus.SEATED;
    }

    public void cancel() {
        if (!this.status.isCancellable()) {
            throw new BusinessException(WaitingCode.WAITING_INVALID_STATE);
        }
        this.status = WaitingStatus.CANCELLED;
    }

    public void noShow() {
        validateStatus(WaitingStatus.CALLED);
        this.status = WaitingStatus.NO_SHOW;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    private void validateStatus(WaitingStatus expected) {
        if (this.status != expected) {
            throw new BusinessException(WaitingCode.WAITING_INVALID_STATE);
        }
    }
}
