package ru.glebdos.ws.logistik.data.entity.postgresql;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deliveries")
@NamedEntityGraph(
        name = "Delivery.withStatusHistory",
        attributeNodes = @NamedAttributeNode("statusHistory")
)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Delivery {
    @Id
    private Long id;
    
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryStatusHistory> statusHistory = new ArrayList<>();

    // Метод для получения текущего статуса
    public DeliveryStatus getCurrentStatus() {
        return statusHistory.isEmpty()
                ? null
                : statusHistory.get(statusHistory.size()-1).getStatus();
    }

    // Метод для добавления нового статуса
    public void addStatusHistory(DeliveryStatus status, Instant timestamp) {
        DeliveryStatusHistory history = new DeliveryStatusHistory();
        history.setDelivery(this);
        history.setStatus(status);
        history.setStatusTimestamp(timestamp);
        statusHistory.add(0, history); // Добавляем в начало списка
    }

    public Delivery(Long id, List<DeliveryStatusHistory> statusHistory) {
        this.id = id;
        this.statusHistory = statusHistory;
    }

    public Delivery() {
    }


}