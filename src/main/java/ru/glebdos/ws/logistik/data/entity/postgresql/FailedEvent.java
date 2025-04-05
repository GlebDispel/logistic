package ru.glebdos.ws.logistik.data.entity.postgresql;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "failed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_id")
    private Long deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status")
    private DeliveryStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private DeliveryStatus toStatus;

    @Column(name = "event_timestamp")
    private Instant eventTimestamp;
    @Column(name = "failure_timestamp")
    private Instant failureTimestamp;
    private String exception;

    public FailedEvent(Long deliveryId, DeliveryStatus currentStatus, DeliveryStatus toStatus, Instant eventTimestamp, Instant failureTimestamp, String exception) {
        this.deliveryId = deliveryId;
        this.currentStatus = currentStatus;
        this.toStatus = toStatus;
        this.eventTimestamp = eventTimestamp;
        this.failureTimestamp = failureTimestamp;
        this.exception = exception;

    }

}
