package ru.glebdos.ws.logistik.data.entity.postgresql;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "delivery_status_history")
@ToString
@EqualsAndHashCode
@NoArgsConstructor
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

}