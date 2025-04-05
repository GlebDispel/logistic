package ru.glebdos.ws.logistik.config.database.postresql;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.liquibase")
@Getter
@Setter
public class PostgresLiquibaseProperties {
    private String changeLog;
    private String defaultSchema;

}