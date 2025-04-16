package ru.glebdos.ws.logistik.unit.entity;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusHistory;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryTest {

    @Test
    @DisplayName("Успешное получение текущего статуса")
    void getCurrentStatus_WhenHistoryHasStatus_ReturnLastStatus(){
        Delivery delivery = new Delivery();

        delivery.addStatusHistory(DeliveryStatus.NEW, Instant.now());
        delivery.addStatusHistory(DeliveryStatus.IN_TRANSIT,Instant.now());

        assertEquals(DeliveryStatus.IN_TRANSIT, delivery.getCurrentStatus());
    }

    @Test
    @DisplayName("Успешное добавление статуса в конец списка")
    void addStatusHistory_AppendsToEndOfList(){
        Delivery delivery = new Delivery();

        delivery.addStatusHistory(DeliveryStatus.NEW, Instant.now());
        delivery.addStatusHistory(DeliveryStatus.IN_TRANSIT,Instant.now());
        delivery.addStatusHistory(DeliveryStatus.ARRIVED,Instant.now());
        delivery.addStatusHistory(DeliveryStatus.DELIVERY,Instant.now());
        delivery.addStatusHistory(DeliveryStatus.COMPLETE,Instant.now());

        List<DeliveryStatusHistory> deliveryStatusHistoryList = delivery.getStatusHistory();

        assertEquals(DeliveryStatus.NEW, deliveryStatusHistoryList.get(0).getStatus());
        assertEquals(DeliveryStatus.IN_TRANSIT, deliveryStatusHistoryList.get(1).getStatus());
        assertEquals(DeliveryStatus.ARRIVED, deliveryStatusHistoryList.get(2).getStatus());
        assertEquals(DeliveryStatus.DELIVERY, deliveryStatusHistoryList.get(3).getStatus());
        assertEquals(DeliveryStatus.COMPLETE, deliveryStatusHistoryList.get(4).getStatus());
    }
}
