package ru.glebdos.ws.logistik.data.repository.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.glebdos.ws.logistik.data.entity.postgresql.FailedEvent;

public interface FailedEventRepository extends JpaRepository<FailedEvent,Long> {
}
