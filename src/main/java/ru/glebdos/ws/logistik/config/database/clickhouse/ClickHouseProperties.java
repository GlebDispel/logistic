package ru.glebdos.ws.logistik.config.database.clickhouse;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.datasource.clickhouse")
@Getter
@Setter
public class ClickHouseProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
