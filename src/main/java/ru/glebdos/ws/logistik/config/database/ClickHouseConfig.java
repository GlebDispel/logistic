package ru.glebdos.ws.logistik.config.database;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class ClickHouseConfig {


    private final Environment environment;

    @Autowired
    public ClickHouseConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean(name = "clickhouseDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(environment.getProperty("spring.datasource.clickhouse.url"));
        dataSource.setUsername(environment.getProperty("spring.datasource.clickhouse.username"));
        dataSource.setPassword(environment.getProperty("spring.datasource.clickhouse.password"));
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.clickhouse.driver-class-name"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate clickhouseJdbcTemplate(@Qualifier("clickhouseDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    @Bean
    public SpringLiquibase clickhouseLiquibase(
            @Qualifier("clickhouseDataSource") DataSource dataSource,
            @Value("${app.liquibase.clickhouse.change-log}") String changeLog) {

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