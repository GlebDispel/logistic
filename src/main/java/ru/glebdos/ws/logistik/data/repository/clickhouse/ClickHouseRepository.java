package ru.glebdos.ws.logistik.data.repository.clickhouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class ClickHouseRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ClickHouseRepository(@Qualifier("clickhouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long getLastId(){
      return   jdbcTemplate.queryForObject("SELECT MAX(historyId) FROM delivery_status_history", Long.class);
    }
}