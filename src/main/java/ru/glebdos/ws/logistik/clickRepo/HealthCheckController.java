package ru.glebdos.ws.logistik.clickRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    
    private final ClickHouseRepository clickHouseRepo;

    @Autowired
    public HealthCheckController(ClickHouseRepository clickHouseRepo) {
        this.clickHouseRepo = clickHouseRepo;
    }

    @GetMapping("/health/clickhouse")
    public String checkClickhouse() {
        try {
            String version = clickHouseRepo.checkConnection();
            return "ClickHouse connection OK. Version: " + version;
        } catch (Exception e) {
            return "ClickHouse connection FAILED: " + e.getMessage();
        }
    }
}