package ru.glebdos.ws.logistik.data.repository.postgresql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.glebdos.ws.logistik.data.entity.postgresql.FailedEvent;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent,Long> {
}
