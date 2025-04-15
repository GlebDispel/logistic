package ru.glebdos.ws.logistik.unit.services;

import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.glebdos.ws.logistik.data.dto.clickhouse.DeliveryStatusHistoryClickHouse;
import ru.glebdos.ws.logistik.services.ClickHouseService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;


@ExtendWith(MockitoExtension.class)
class ClickHouseServiceTest {

    @InjectMocks
    ClickHouseService clickHouseService;

    @Mock
    JdbcTemplate clickHouseJdbcTemplate;

    @Captor
    private ArgumentCaptor<BatchPreparedStatementSetter> setterCaptor;

    private final String sql = "INSERT INTO delivery_status_history (historyId, delivery_id, status, statusTimestamp, slaViolated, violationDuration) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    @Test
    @SneakyThrows
    @DisplayName("Успешная батч запись")
    void batchInsert_WhenDataIsNotEmpty_ExecutesBatchUpdate() {

        //Given

        List<DeliveryStatusHistoryClickHouse> data = List.of(
                new DeliveryStatusHistoryClickHouse(1L, 100L, "NEW", Instant.now(), 1, 60L),
                new DeliveryStatusHistoryClickHouse(2L, 101L, "ARRIVED", Instant.now(), 0, 0L)
        );

        //when
        clickHouseService.batchInsert(data);

        //Then
        verify(clickHouseJdbcTemplate).batchUpdate(eq(sql), setterCaptor.capture());

        BatchPreparedStatementSetter setter = setterCaptor.getValue();
        assertAll(
                () -> assertEquals(data.size(), setter.getBatchSize()),
                () -> {
                    PreparedStatement ps = mock(PreparedStatement.class);
                    setter.setValues(ps, 0);
                    verify(ps).setLong(1, 1L);
                    verify(ps).setLong(2, 100L);
                    verify(ps).setString(3, "NEW");
                    verify(ps).setTimestamp(eq(4), any(Timestamp.class));
                    verify(ps).setInt(5, 1);
                    verify(ps).setLong(6, 60L);
                },
                () -> {
                    PreparedStatement ps = mock(PreparedStatement.class);
                    setter.setValues(ps, 1);
                    verify(ps).setLong(1, 2L);
                    verify(ps).setLong(2, 101L);
                    verify(ps).setString(3, "ARRIVED");
                    verify(ps).setTimestamp(eq(4), any(Timestamp.class));
                    verify(ps).setInt(5, 0);
                    verify(ps).setLong(6, 0L);
                }
        );
    }

    @Test
    @DisplayName("При пустом списке, батч вставка не будет вызвана")
    void batchInsert_WhenDataIsEmpty_DoesNothing() {
        clickHouseService.batchInsert(Collections.emptyList());
        verify(clickHouseJdbcTemplate, never()).batchUpdate(eq(sql), any(BatchPreparedStatementSetter.class));
    }

    @Test
    @DisplayName("При ошибке, иключение будет проброшено из сервиса")
    void batchInsert_WhenJdbcTemplateThrowsException_PropagatesIt() {
        // Given
        when(clickHouseJdbcTemplate.batchUpdate(eq(sql), any(BatchPreparedStatementSetter.class)))
                .thenThrow(new RuntimeException("DB error"));

        // Then
        assertThrows(RuntimeException.class,
                () -> clickHouseService.batchInsert(List.of(new DeliveryStatusHistoryClickHouse())));
    }


}