package ru.glebdos.ws.logistik.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.entity.postgresql.FailedEvent;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryRepository;
import ru.glebdos.ws.logistik.data.repository.postgresql.FailedEventRepository;
import ru.glebdos.ws.logistik.services.DeliveryStatusService;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RetryManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final int MAX_ATTEMPTS = 3;
    private final DeliveryStatusService deliveryStatusService;
    private final FailedEventRepository failedEventRepository;
    private final DeliveryRepository deliveryRepository;

    @Autowired
    public RetryManager(DeliveryStatusService deliveryStatusService, FailedEventRepository failedEventRepository, DeliveryRepository deliveryRepository) {
        this.deliveryStatusService = deliveryStatusService;
        this.failedEventRepository = failedEventRepository;
        this.deliveryRepository = deliveryRepository;
    }

    public void scheduleRetry(ConsumerRecord<String, DeliveryStatusMessage> record, int attempt) {
        scheduler.schedule(() -> {
            try {

                log.info("Retry attempt #{} for {}", attempt, record.key());
                deliveryStatusService.processStatusUpdate(record.value());
                log.info("Повторная попытка №{} успешна для ключа: {}", attempt, record.key());
            } catch (Exception ex) {
                log.error("Retry failed: ", ex);
                if (attempt < MAX_ATTEMPTS) {
                    log.warn("Попытка №{} не удалась. Планирую следующую...", attempt);
                    scheduleRetry(record, attempt + 1); // Рекурсивный вызов
                } else {
                    log.error("Максимальное число попыток достигнуто. Сохраняю в failed events.");
                    failedEventRepository.save(new FailedEvent(record.value().getId(),
                                    record.value().getCurrentStatus(),
                                    record.value().getToStatus(),
                                    record.value().getTimestamp(),
                                    Instant.now(),
                                    ex.toString())
                            );
                    // сохраняем в БД или лог
                }
            }
        }, getDelayForAttempt(attempt), TimeUnit.SECONDS);
    }

    private long getDelayForAttempt(int attempt) {
        return switch (attempt) {
            case 1 -> 10;
            case 2 -> 30;
            default -> 60;
        };
    }
}