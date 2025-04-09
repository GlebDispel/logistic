package ru.glebdos.ws.logistik.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DeliveryStatusIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private KafkaTemplate<String, DeliveryStatusMessage> kafkaTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Test
    public void testKafkaMessageProcessedAndSavedToPostgres()  {
        // given
        DeliveryStatusMessage message = new DeliveryStatusMessage(
                1L,
                null,
                DeliveryStatus.NEW,
                Instant.now()
        );

        // when
        kafkaTemplate.send("delivery-status-updates",  message);

        // then
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    Optional<Delivery> deliveryOpt = deliveryRepository.findById(1L);
                    assertThat(deliveryOpt).isPresent();
                    assertThat(deliveryOpt.get().getCurrentStatus()).isEqualTo(DeliveryStatus.NEW);
                });
    }

}