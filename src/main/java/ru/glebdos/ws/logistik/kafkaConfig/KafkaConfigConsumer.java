package ru.glebdos.ws.logistik.kafkaConfig;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.glebdos.ws.logistik.entityPostgre.DeliveryStatusMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class KafkaConfigConsumer {

    @Bean
    public ConsumerFactory<String, DeliveryStatusMessage> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9094,localhost:9096");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "delivery_group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(DeliveryStatusMessage.class)
        );
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        // Добавляем исключения которые НЕ должны повторяться
        DefaultErrorHandler handler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Final error after retries", exception);
                },
                new FixedBackOff(1000L, 2L)
        );

        handler.addNotRetryableExceptions(IllegalStateException.class);
        return handler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DeliveryStatusMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler());
        return factory;
    }
}