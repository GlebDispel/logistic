package ru.glebdos.ws.logistik.services;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.glebdos.ws.logistik.data.dto.clickhouse.DeliveryStatusHistoryClickHouse;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClickHouseService {
    
    private final JdbcTemplate clickhouseJdbcTemplate;

    public void batchInsert(List<DeliveryStatusHistoryClickHouse> data) {
        if (data.isEmpty())
            return;

        String sql = "INSERT INTO delivery_status_history (historyId, delivery_id, status, statusTimestamp, slaViolated, violationDuration) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        clickhouseJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DeliveryStatusHistoryClickHouse item = data.get(i);
                ps.setLong(1, item.getHistoryId());
                ps.setLong(2, item.getDeliveryId());
                ps.setString(3, item.getStatus());
                ps.setTimestamp(4, Timestamp.from(item.getStatusTimestamp()));
                ps.setInt(5, item.getSlaViolated()); // 1/0
                ps.setLong(6, item.getViolationDuration());
            }

            @Override
            public int getBatchSize() {
                return data.size();
            }
        });
    }
}