package ru.glebdos.ws.logistik.entityPostgre;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DeliveryStatusMessage {
    private Long id;
    private DeliveryStatus currentStatus; // Может быть null
    private DeliveryStatus toStatus;
    private Instant timestamp;


    public DeliveryStatusMessage(Long id, DeliveryStatus currentStatus, DeliveryStatus toStatus, Instant timestamp) {
        this.id = id;
        this.currentStatus = currentStatus;
        this.toStatus = toStatus;
        this.timestamp = timestamp;
    }

    public DeliveryStatusMessage() {
    }


}