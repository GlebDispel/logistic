package ru.glebdos.ws.logistik.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.glebdos.ws.logistik.entityPostgre.FailedEvent;

public interface FailedEventRepository extends JpaRepository<FailedEvent,Long> {
}
