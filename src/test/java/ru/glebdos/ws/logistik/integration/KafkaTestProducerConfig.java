package ru.glebdos.ws.logistik.integration;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class KafkaTestProducerConfig {

    @Bean
    public ProducerFactory<String, DeliveryStatusMessage> testProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, DeliveryStatusMessage> kafkaTestTemplate(
            ProducerFactory<String, DeliveryStatusMessage> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}