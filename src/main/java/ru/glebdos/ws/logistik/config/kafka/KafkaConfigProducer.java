package ru.glebdos.ws.logistik.config.kafka;


import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfigProducer {



    Map<String,Object> producerConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9094,localhost:9096");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return config;
    }

        ProducerFactory<String, DeliveryStatusMessage> producerFactoryy() {
        return new DefaultKafkaProducerFactory<>(producerConfig());
    }

    @Bean(name = "tempProducer")
    KafkaTemplate<String, DeliveryStatusMessage> kafkaTemplate() {
        KafkaTemplate<String, DeliveryStatusMessage> template = new KafkaTemplate<>(producerFactoryy());
        template.setObservationEnabled(true); // Для трейсинга
        return template;
    }

    @Bean
    NewTopic createTopic(){
        return TopicBuilder
                .name("delivery-status-updates")
                .partitions(3)
                .replicas(3)
                .configs(Map.of("min.insync.replicas","2"))
                .build();
    }
}
