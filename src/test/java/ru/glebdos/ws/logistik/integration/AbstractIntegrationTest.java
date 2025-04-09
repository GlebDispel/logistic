package ru.glebdos.ws.logistik.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.clickhouse.ClickHouseContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@Import(KafkaTestProducerConfig.class)
public abstract class AbstractIntegrationTest {

    @Container
    public static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("sla")
            .withUsername("test")
            .withPassword("test");

    @Container
    public static ClickHouseContainer clickhouse = new ClickHouseContainer("clickhouse/clickhouse-server:latest")
            .withDatabaseName("default")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("DATASOURCE_POSTGRES_URL", postgres::getJdbcUrl);
        registry.add("DATASOURCE_POSTGRES_USERNAME", postgres::getUsername);
        registry.add("DATASOURCE_POSTGRES_PASSWORD", postgres::getPassword);
        registry.add("DATASOURCE_POSTGRES_DRIVER", () -> "org.postgresql.Driver");

        registry.add("DATASOURCE_CLICKHOUSE_URL", clickhouse::getJdbcUrl);
        registry.add("DATASOURCE_CLICKHOUSE_USERNAME", clickhouse::getUsername);
        registry.add("DATASOURCE_CLICKHOUSE_PASSWORD", clickhouse::getPassword);
        registry.add("DATASOURCE_CLICKHOUSE_DRIVER", () -> "com.clickhouse.jdbc.ClickHouseDriver");
    }
}