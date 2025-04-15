package ru.glebdos.ws.logistik.unit.repository;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.glebdos.ws.logistik.data.repository.clickhouse.ClickHouseRepository;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ClickHouseRepositoryTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    @InjectMocks
    ClickHouseRepository clickHouseRepository;

    private final String sql = "SELECT MAX(historyId) FROM delivery_status_history";


    @Test
    @DisplayName("возвращает null, если таблица пустая")
    void getLastId_WhenTableIsEmpty_ReturnsNull(){

        when(jdbcTemplate.queryForObject(eq(sql),eq(Long.class)))
                .thenReturn(null);

        Long result = clickHouseRepository.getLastId();
        assertNull(result);
    }

    @Test
    @DisplayName("успешное возвращение последнего id")
    void  getLastId_WhenTableHasData_ReturnsMaxId(){
        when(jdbcTemplate.queryForObject(eq(sql),eq(Long.class)))
                .thenReturn(100L);

        Long result = clickHouseRepository.getLastId();
        assertEquals(100L,result);
    }
    @Test
    @DisplayName("Успешно пробасывает исключение")
    void getLastId_WhenJdbcTemplateThrowsException_PropagatesIt() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> clickHouseRepository.getLastId());
    }

}