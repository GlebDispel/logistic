package ru.glebdos.ws.logistik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
@EntityScan("ru.glebdos.ws.logistik.entityPostgre")
public class LogistikApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogistikApplication.class, args);
    }

}
