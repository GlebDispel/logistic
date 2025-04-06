package ru.glebdos.ws.logistik.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.glebdos.ws.logistik.data.dto.clickhouse.DeliveryStatusHistoryClickHouse;
import ru.glebdos.ws.logistik.data.repository.clickhouse.ClickHouseRepository;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusHistory;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryStatusHistoryRepository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataSyncScheduler {


    private final Pageable pageable = PageRequest.of(0, 1, Sort.by("statusTimestamp").descending());
    private final DeliveryStatusHistoryRepository postgresRepo;
    private final ClickHouseService clickhouseService;
    private final ClickHouseRepository clickHouseRepository;
    private static final Map<DeliveryStatus, Duration> SLA_LIMITS = Map.of(
            DeliveryStatus.NEW, Duration.ofMinutes(1),
            DeliveryStatus.IN_TRANSIT, Duration.ofMinutes(1),
            DeliveryStatus.ARRIVED, Duration.ofMinutes(1),
            DeliveryStatus.DELIVERY, Duration.ofMinutes(1)
    );

    @Scheduled(cron = "0 */1 * * * *")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000), retryFor = {DataAccessException.class})
    public void syncData() {
        try {
            // Этап 1: Получение lastId
            Long lastId;
            try {
                lastId = clickHouseRepository.getLastId() + 1;
            } catch (Exception e) {
                log.error("Ошибка при получении lastId: {}", e.getMessage());
                return;
            }

            // Этап 2: Запрос данных из PostgreSQL
            List<DeliveryStatusHistory> postgresData;
            try {
                postgresData = postgresRepo.findRecent(lastId);
            } catch (Exception e) {
                log.error("Ошибка при запросе данных из PostgreSQL: {}", e.getMessage());
                return;
            }

            // Этап 3: Конвертация
            List<DeliveryStatusHistoryClickHouse> clickhouseData = postgresData.stream()
                    .map(this::convertToClickHouseDto)
                    .collect(Collectors.toList());

            log.info("Конвертировано {} записей", clickhouseData.size());

            // Этап 4: Вставка в ClickHouse
            if (!clickhouseData.isEmpty()) {
                try {
                    clickhouseService.batchInsert(clickhouseData);
                    log.info("Вставка прошла успешно");
                } catch (Exception e) {
                    log.error("Ошибка вставки в ClickHouse: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Critical error in syncData: {}", e.getMessage(), e);
        }
    }

    private DeliveryStatusHistoryClickHouse convertToClickHouseDto(DeliveryStatusHistory entity) {
        DeliveryStatusHistoryClickHouse dto = new DeliveryStatusHistoryClickHouse();
        dto.setHistoryId(entity.getHistoryId());
        dto.setDeliveryId(entity.getDelivery().getId());
        dto.setStatus(entity.getStatus().name());
        dto.setStatusTimestamp(entity.getStatusTimestamp());


        // Получаем предыдущий статус для этой же доставки
        Page<DeliveryStatusHistory> prevStatusOpt = postgresRepo.findPreviousStatus(
                entity.getDelivery().getId(),
                entity.getStatusTimestamp(),
                pageable
        );

        if (!prevStatusOpt.getContent().isEmpty()) {
            DeliveryStatusHistory prevStatus = prevStatusOpt.getContent().get(0);
            Duration timeSpent = Duration.between(
                    prevStatus.getStatusTimestamp(),
                    entity.getStatusTimestamp()
            );

            // Проверяем, есть ли лимит на этот статус
            Duration slaLimit = SLA_LIMITS.getOrDefault(prevStatus.getStatus(), Duration.ZERO);
            boolean slaViolated = timeSpent.compareTo(slaLimit) > 0;

            // Устанавливаем значения с преобразованием типов
            dto.setSlaViolated(slaViolated ? 1 : 0); // 1 = true, 0 = false
            dto.setViolationDuration(
                    slaViolated
                            ? timeSpent.minus(slaLimit).getSeconds()
                            : 0
            );
        } else {
            // Для первого статуса нарушения SLA нет
            dto.setSlaViolated(0);
            dto.setViolationDuration(0L);
        }

        return dto;
    }
}