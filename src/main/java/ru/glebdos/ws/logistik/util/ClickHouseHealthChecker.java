package ru.glebdos.ws.logistik.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class ClickHouseHealthChecker {
    private final DataSource clickhouseDataSource;

    @Autowired
    public ClickHouseHealthChecker(@Qualifier("clickhouseDataSource") DataSource clickhouseDataSource) {
        this.clickhouseDataSource = clickhouseDataSource;
    }

    @Scheduled(fixedRate = 300000)
    public void checkConnection() {
        try (Connection conn = clickhouseDataSource.getConnection()) {
            boolean isValid = conn.isValid(5);
            System.out.println("ClickHouse connection valid: " + isValid);
        } catch (Exception e) {
            System.err.println("ClickHouse connection error: " + e.getMessage());
        }
    }
}