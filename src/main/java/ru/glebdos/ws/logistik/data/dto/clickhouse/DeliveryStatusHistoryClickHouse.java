package ru.glebdos.ws.logistik.data.dto.clickhouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryStatusHistoryClickHouse {
    private Long historyId;
    private Long deliveryId;
    private String status;
    private Instant statusTimestamp;

    private Integer slaViolated; // Было ли нарушение SLA
    private Long violationDuration; // Время превышения SLA

}