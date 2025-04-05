package ru.glebdos.ws.logistik.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.entity.postgresql.FailedEvent;
import ru.glebdos.ws.logistik.data.repository.postgresql.FailedEventRepository;
import ru.glebdos.ws.logistik.services.DeliveryStatusService;
import ru.glebdos.ws.logistik.util.RetryManager;

import java.time.Instant;



@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryStatusConsumer {

    private final RetryManager retryManager;
    private final DeliveryStatusService statusService;
    private final FailedEventRepository failedEventRepository;

    @KafkaListener(topics = "delivery-status-updates")
    public void handleStatusUpdate(
            ConsumerRecord<String, DeliveryStatusMessage> record,
            Acknowledgment acknowledgment) {

        try {
            log.info("Received message: {}", record.value());
            statusService.processStatusUpdate(record.value());
            acknowledgment.acknowledge();
        } catch (IllegalStateException e) {
            // Фатальные бизнес-ошибки
            log.error("Non-retryable error: {}", e.getMessage());
            failedEventRepository.save(new FailedEvent(record.value().getId(),
                    record.value().getCurrentStatus(),
                    record.value().getToStatus(),
                    record.value().getTimestamp(),
                    Instant.now(),
                    e.toString()
            ));
            acknowledgment.acknowledge();
        } catch (Exception e) {
            // Временные ошибки для ручных ретраев
            log.error("Retryable error: {}", e.getMessage());
            retryManager.scheduleRetry(record, 1);
            acknowledgment.acknowledge();
        }
    }
}