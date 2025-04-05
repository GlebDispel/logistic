package ru.glebdos.ws.logistik.data.repository.postgresql;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    @EntityGraph(value = "Delivery.withStatusHistory", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Delivery> findWithStatusHistoryById(Long id);

    // Стандартный метод с графом
    @Override
    @EntityGraph("Delivery.withStatusHistory")
    Optional<Delivery> findById(Long id);
}
