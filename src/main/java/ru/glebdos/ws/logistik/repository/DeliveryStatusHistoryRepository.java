package ru.glebdos.ws.logistik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.glebdos.ws.logistik.entityPostgre.DeliveryStatusHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {
    
    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.historyId >= :history_id")
    List<DeliveryStatusHistory> findRecent(@Param("history_id") Long history_id);

    @Query("SELECT dsh FROM DeliveryStatusHistory dsh " +
            "WHERE dsh.delivery.id = :deliveryId " +
            "AND dsh.statusTimestamp < :currentTimestamp " +
            "ORDER BY dsh.statusTimestamp DESC LIMIT 1")
    Optional<DeliveryStatusHistory> findPreviousStatus(@Param("deliveryId") Long deliveryId,
                                                       @Param("currentTimestamp") Instant currentTimestamp);
}