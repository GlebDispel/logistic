package ru.glebdos.ws.logistik.data.entity.postgresql;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeliveryStatusMessage {
    private Long id;
    private DeliveryStatus currentStatus; // Может быть null
    private DeliveryStatus toStatus;
    private Instant timestamp;


}