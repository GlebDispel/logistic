package ru.glebdos.ws.logistik.config.database.clickhouse;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {

    @Value("${app.liquibase.clickhouse.change-log}")
    private String changeLog;
    private final ClickHouseProperties properties;

    public ClickHouseConfig(ClickHouseProperties properties) {
        this.properties = properties;
    }


    @Bean(name = "clickhouseDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setDriverClassName(properties.getDriverClassName());
        return dataSource;
    }

    @Bean
    public JdbcTemplate clickhouseJdbcTemplate(@Qualifier("clickhouseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    @Bean
    public SpringLiquibase clickhouseLiquibase(
            @Qualifier("clickhouseDataSource") DataSource dataSource) {

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(changeLog);
        liquibase.setDefaultSchema("default"); // Явно указываем схему
        liquibase.setLiquibaseSchema("default"); // Отключаем создание таблиц Liquibase в другой схеме
        liquibase.setShouldRun(true);
        liquibase.setTestRollbackOnUpdate(false);
        return liquibase;
    }
}