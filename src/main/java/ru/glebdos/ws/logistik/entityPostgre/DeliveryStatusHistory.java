package ru.glebdos.ws.logistik.entityPostgre;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "delivery_status_history")
public class DeliveryStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;
    
    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    @Column(name = "status_timestamp")
    private Instant statusTimestamp;


    public DeliveryStatusHistory(Delivery delivery, DeliveryStatus status, Instant statusTimestamp) {
        this.delivery = delivery;
        this.status = status;
        this.statusTimestamp = statusTimestamp;
    }

    public DeliveryStatusHistory() {
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryStatusHistory that = (DeliveryStatusHistory) o;
        return Objects.equals(historyId, that.historyId) && Objects.equals(delivery, that.delivery) && status == that.status && Objects.equals(statusTimestamp, that.statusTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId, delivery, status, statusTimestamp);
    }
}