package ru.glebdos.ws.logistik.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.glebdos.ws.logistik.entityPostgre.Delivery;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    @EntityGraph(value = "Delivery.withStatusHistory", type = EntityGraph.EntityGraphType.LOAD)
    Optional<Delivery> findWithStatusHistoryById(Long id);

    // Стандартный метод с графом
    @Override
    @EntityGraph("Delivery.withStatusHistory")
    Optional<Delivery> findById(Long id);
}
