package ru.glebdos.ws.logistik.util;




import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.web.bind.annotation.*;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;


import java.time.Instant;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

@RestController
@RequestMapping("kek")
public class Sender {

    private final KafkaTemplate<String, DeliveryStatusMessage> kafkaTemplate;


    public Sender(@Qualifier("tempProducer") KafkaTemplate<String, DeliveryStatusMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;

    }

//    @PostMapping
//    public ResponseEntity<?> sender(@RequestParam("id") Long id,
//                                   @RequestParam(value = "currentStatus", required = false) DeliveryStatus currentStatus,
//                                   @RequestParam("toStatus") DeliveryStatus toStatus) throws ExecutionException, InterruptedException {
//
//
//        DeliveryStatusMessage deliveryStatusMessage = new DeliveryStatusMessage(id,currentStatus,toStatus, now);
//        kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage);
//
//        return ResponseEntity.ok("ok");
//    }


    @GetMapping("/test")
    public void test() throws InterruptedException {

        long start = 100;

        for (long i = 0; i < start; i++) {
            Random random = new Random();
            DeliveryStatusMessage deliveryStatusMessage = new DeliveryStatusMessage(
                    i,null,DeliveryStatus.NEW, time());
            kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage);
            Thread.sleep(random.nextLong(100000));
            DeliveryStatusMessage deliveryStatusMessage1 = new DeliveryStatusMessage(
                    i,DeliveryStatus.NEW,DeliveryStatus.IN_TRANSIT, time());
            kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage1);
            Thread.sleep(random.nextLong(100000));
            DeliveryStatusMessage deliveryStatusMessage2 = new DeliveryStatusMessage(
                    i,DeliveryStatus.IN_TRANSIT,DeliveryStatus.ARRIVED, time());
            kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage2);
            Thread.sleep(random.nextLong(100000));
            DeliveryStatusMessage deliveryStatusMessage3 = new DeliveryStatusMessage(
                    i,DeliveryStatus.ARRIVED,DeliveryStatus.DELIVERY, time());
            kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage3);
            Thread.sleep(random.nextLong(100000));
            DeliveryStatusMessage deliveryStatusMessage4 = new DeliveryStatusMessage(
                    i,DeliveryStatus.DELIVERY,DeliveryStatus.COMPLETE, time());
            kafkaTemplate.send("delivery-status-updates",deliveryStatusMessage4);
            Thread.sleep(random.nextLong(100000));

        }



    }
    private Instant time(){
        ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        return localTime.toInstant();
    }


}
