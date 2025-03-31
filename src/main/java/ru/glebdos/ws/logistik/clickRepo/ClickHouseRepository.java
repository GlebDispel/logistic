package ru.glebdos.ws.logistik.clickRepo;

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
    public String checkConnection() {
        return jdbcTemplate.queryForObject(
                "SELECT version()",  // ClickHouse поддерживает эту функцию
                String.class
        );
    }

    public Long getLastId(){
      return   jdbcTemplate.queryForObject("SELECT MAX(historyId) FROM delivery_status_history", Long.class);
    }
}