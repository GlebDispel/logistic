package ru.glebdos.ws.logistik.config.database.postresql;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.datasource.postgres")
@Getter
@Setter
public class PostgresProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
