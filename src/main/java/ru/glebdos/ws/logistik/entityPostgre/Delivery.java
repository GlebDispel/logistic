package ru.glebdos.ws.logistik.entityPostgre;


import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "deliveries")
@NamedEntityGraph(
        name = "Delivery.withStatusHistory",
        attributeNodes = @NamedAttributeNode("statusHistory")
)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<DeliveryStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<DeliveryStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "id=" + id +
                ", statusHistory=" + statusHistory +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return Objects.equals(id, delivery.id) && Objects.equals(statusHistory, delivery.statusHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, statusHistory);
    }
}