package ru.glebdos.ws.logistik.unit.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import ru.glebdos.ws.logistik.data.dto.clickhouse.DeliveryStatusHistoryClickHouse;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusHistory;
import ru.glebdos.ws.logistik.data.repository.clickhouse.ClickHouseRepository;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryStatusHistoryRepository;
import ru.glebdos.ws.logistik.services.ClickHouseService;
import ru.glebdos.ws.logistik.services.DataSyncScheduler;


import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DataSyncSchedulerTest {

    @InjectMocks
    DataSyncScheduler scheduler;

    @Mock
    ClickHouseRepository clickHouseRepository;

    @Mock
    DeliveryStatusHistoryRepository postgresRepository;

    @Mock
    ClickHouseService clickHouseService;

    private Instant now;

    @BeforeEach
    void setup() {
        now = Instant.now();
        when(clickHouseRepository.getLastId()).thenReturn(100L);
    }

    private Delivery createDelivery(long id) {
        Delivery d = new Delivery();
        d.setId(id);
        return d;
    }
    private DeliveryStatusHistory createStatus(long historyId, Delivery delivery, DeliveryStatus status, Instant ts) {
        DeliveryStatusHistory h = new DeliveryStatusHistory();
        h.setHistoryId(historyId);
        h.setDelivery(delivery);
        h.setStatus(status);
        h.setStatusTimestamp(ts);
        return h;
    }


    @Test
    @DisplayName("Если getLastId выбрасывает исключение - логируем и выходим")
    void syncData_WhenGetLastIdThrowException_ShouldLogErrorAndReturn(){
        // given
        when(clickHouseRepository.getLastId()).thenThrow(new RuntimeException("clickhouse error"));
        // when
        scheduler.syncData();
        // then
        verify(postgresRepository, never()).findRecent(anyLong());
        verify(clickHouseService, never()).batchInsert(anyList());

    }

    @Test
    @DisplayName("Если findRecent выбрасывает исключение - логгируем и выходим")
    void syncData_WhenFindRecentThrowException_ShouldLogErrorAndReturn(){
        // given
        when(postgresRepository.findRecent(101L)).thenThrow(new RuntimeException("Postgres error"));
        // when
        scheduler.syncData();
        // then
        verify(clickHouseRepository).getLastId();
        verify(postgresRepository).findRecent(101L);
        verify(clickHouseService, never()).batchInsert(anyList());

    }
    @Test
    @DisplayName("Если ClickHouse вставка выбрасывает исключение — логгируем и продолжаем")
    void syncData_WhenClickHouseInsertThrowsException_ShouldLogErrorAndContinue() {
        // given
        Delivery delivery = createDelivery(200L);

        DeliveryStatusHistory entity = createStatus(101L,delivery,DeliveryStatus.NEW,now);


        when(postgresRepository.findRecent(101L)).thenReturn(List.of(entity));
        when(postgresRepository.findPreviousStatus(eq(200L), any(), any())).thenReturn(Page.empty());

        doThrow(new RuntimeException("ClickHouse insert error"))
                .when(clickHouseService).batchInsert(anyList());

        // when
        assertDoesNotThrow(() -> scheduler.syncData());

        // then
        verify(clickHouseRepository).getLastId();
        verify(postgresRepository).findRecent(101L);
        verify(clickHouseService).batchInsert(anyList());
    }

    @Test
    @DisplayName("Все данные валидны - успешная конвертация и вставка")
    void syncData_WhenEverythingIsOk_ShouldConvertAndInsertData(){
        // given

        Delivery delivery = createDelivery(200L);

        DeliveryStatusHistory current = createStatus(101L,delivery,DeliveryStatus.ARRIVED,now);

        DeliveryStatusHistory previous = createStatus(100L,delivery,DeliveryStatus.IN_TRANSIT,
                current.getStatusTimestamp().minus(Duration.ofMinutes(2)));


        when(postgresRepository.findRecent(101L)).thenReturn(List.of(current));
        when(postgresRepository.findPreviousStatus(eq(200L),any(),any()))
                .thenReturn(new PageImpl<>(List.of(previous)));

        // when
        scheduler.syncData();

        // then
        verify(clickHouseRepository).getLastId();
        verify(postgresRepository).findRecent(101L);
        verify(postgresRepository).findPreviousStatus(eq(200L), any(), any());

        ArgumentCaptor<List<DeliveryStatusHistoryClickHouse>> captor = ArgumentCaptor.forClass(List.class);
        verify(clickHouseService).batchInsert(captor.capture());

        List<DeliveryStatusHistoryClickHouse> inserted = captor.getValue();
        assertEquals(1,inserted.size());

        DeliveryStatusHistoryClickHouse insertedDto = inserted.get(0);

        assertEquals(101L, insertedDto.getHistoryId());
        assertEquals(200L, insertedDto.getDeliveryId());
        assertEquals("ARRIVED", insertedDto.getStatus());
        assertEquals(1,insertedDto.getSlaViolated());
        assertEquals(60L,insertedDto.getViolationDuration());
    }

    @Test
    @DisplayName("Если SLA не нарушено — флаги slaViolated=0 и violationDuration=0")
    void syncData_WhenSlaNotViolated_ShouldSetZeroFlags() {
        // given

        Delivery delivery = createDelivery(200L);

        DeliveryStatusHistory current = createStatus(101L,delivery,DeliveryStatus.ARRIVED,now);

        DeliveryStatusHistory previous = createStatus(100L,delivery,DeliveryStatus.IN_TRANSIT,
                current.getStatusTimestamp().minus(Duration.ofSeconds(30)));

        when(postgresRepository.findRecent(101L)).thenReturn(List.of(current));
        when(postgresRepository.findPreviousStatus(eq(200L),any(),any()))
                .thenReturn(new PageImpl<>(List.of(previous)));

        // when
        scheduler.syncData();

        // then
        ArgumentCaptor<List<DeliveryStatusHistoryClickHouse>> captor = ArgumentCaptor.forClass(List.class);
        verify(clickHouseService).batchInsert(captor.capture());

        List<DeliveryStatusHistoryClickHouse> inserted = captor.getValue();
        assertEquals(1,inserted.size());

        DeliveryStatusHistoryClickHouse insertedDto = inserted.get(0);

        assertEquals(0,insertedDto.getSlaViolated());
        assertEquals(0,insertedDto.getViolationDuration());
    }

    @DisplayName("Если нет предыдущего статуса — slaViolated=0, violationDuration=0")
    @Test
    void syncData_WhenNoPreviousStatus_ShouldSetZeroFlags() {
        // given

        Delivery delivery = createDelivery(200L);

        DeliveryStatusHistory current = createStatus(101L,delivery,DeliveryStatus.ARRIVED,now);


        when(postgresRepository.findRecent(101L)).thenReturn(List.of(current));
        when(postgresRepository.findPreviousStatus(eq(200L),any(),any()))
                .thenReturn(Page.empty());

        // when
        scheduler.syncData();

        // then
        ArgumentCaptor<List<DeliveryStatusHistoryClickHouse>> captor = ArgumentCaptor.forClass(List.class);
        verify(clickHouseService).batchInsert(captor.capture());


        DeliveryStatusHistoryClickHouse insertedDto = captor.getValue().get(0);

        assertEquals(0,insertedDto.getSlaViolated());
        assertEquals(0,insertedDto.getViolationDuration());
    }

    @Test
    @DisplayName("Если findRecent вернул пустой список — batchInsert не вызывается")
    void syncData_WhenFindRecentReturnsEmptyList_ShouldNotInsertAnything() {
        when(postgresRepository.findRecent(101L)).thenReturn(Collections.emptyList());

        scheduler.syncData();

        verify(clickHouseService, never()).batchInsert(anyList());
    }

    @Test
    @DisplayName("Несколько записей — одна с нарушением SLA, одна без")
    void syncData_WhenMultipleRecords_ShouldHandleMixedSla() {

        Delivery delivery1 = createDelivery(200L);


        DeliveryStatusHistory current1 = createStatus(101L,delivery1,DeliveryStatus.ARRIVED,now);


        DeliveryStatusHistory previous1 = createStatus(100L,delivery1,DeliveryStatus.IN_TRANSIT,
                current1.getStatusTimestamp().minus(Duration.ofMinutes(2)));


        Delivery delivery2 = createDelivery(201L);

        DeliveryStatusHistory current2 = createStatus(102L,delivery2,DeliveryStatus.ARRIVED,now);


        DeliveryStatusHistory previous2 = createStatus(101L,delivery2,DeliveryStatus.IN_TRANSIT,
                current1.getStatusTimestamp().minus(Duration.ofSeconds(30)));



        when(postgresRepository.findRecent(101L)).thenReturn(List.of(current1, current2));
        when(postgresRepository.findPreviousStatus(eq(200L), any(), any())).thenReturn(new PageImpl<>(List.of(previous1)));
        when(postgresRepository.findPreviousStatus(eq(201L), any(), any())).thenReturn(new PageImpl<>(List.of(previous2)));

        // when
        scheduler.syncData();

        // then
        ArgumentCaptor<List<DeliveryStatusHistoryClickHouse>> captor = ArgumentCaptor.forClass(List.class);
        verify(clickHouseService).batchInsert(captor.capture());

        List<DeliveryStatusHistoryClickHouse> inserted = captor.getValue();
        assertEquals(2, inserted.size());

        DeliveryStatusHistoryClickHouse dto1 = inserted.stream().filter(d -> d.getDeliveryId() == 200L).findFirst().get();
        assertEquals(1, dto1.getSlaViolated());
        assertEquals(60L, dto1.getViolationDuration());

        DeliveryStatusHistoryClickHouse dto2 = inserted.stream().filter(d -> d.getDeliveryId() == 201L).findFirst().get();
        assertEquals(0, dto2.getSlaViolated());
        assertEquals(0, dto2.getViolationDuration());
    }

}