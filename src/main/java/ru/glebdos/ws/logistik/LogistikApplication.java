package ru.glebdos.ws.logistik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties
public class LogistikApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogistikApplication.class, args);



    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }

}
