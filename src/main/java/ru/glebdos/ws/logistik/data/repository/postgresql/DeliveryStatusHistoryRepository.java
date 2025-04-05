package ru.glebdos.ws.logistik.data.repository.postgresql;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusHistory;


import java.time.Instant;
import java.util.List;

@Repository
public interface DeliveryStatusHistoryRepository extends JpaRepository<DeliveryStatusHistory, Long> {

    @Query("SELECT h FROM DeliveryStatusHistory h WHERE h.historyId >= :history_id")
    List<DeliveryStatusHistory> findRecent(@Param("history_id") Long history_id);

    @Query("SELECT dsh FROM DeliveryStatusHistory dsh " +
            "WHERE dsh.delivery.id = :deliveryId " +
            "AND dsh.statusTimestamp < :currentTimestamp")
    Page<DeliveryStatusHistory> findPreviousStatus(
            @Param("deliveryId") Long deliveryId,
            @Param("currentTimestamp") Instant currentTimestamp,
            Pageable pageable
    );


}