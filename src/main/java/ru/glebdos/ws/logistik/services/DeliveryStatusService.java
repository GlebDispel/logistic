package ru.glebdos.ws.logistik.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryRepository;
import ru.glebdos.ws.logistik.machine.DeliveryStateMachineBuilder;

import java.util.ArrayList;


@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryStatusService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryStateMachineBuilder stateMachineBuilder;



    @Transactional
    public void processStatusUpdate(DeliveryStatusMessage message) {


        log.info("processStatusUpdate : {}", message.toString());

        // 1. Получаем или создаем доставку
        Delivery delivery = deliveryRepository.findWithStatusHistoryById(message.getId())
                .orElseGet(() -> new Delivery(message.getId(),new ArrayList<>()));

        log.info("Delivery получен или создан {} + currentStatus {}",delivery.getId(), delivery.getCurrentStatus());

        // 2. Для новых заказов просто сохраняем статус NEW
        if (message.getToStatus() == DeliveryStatus.NEW) {
            if (delivery.getCurrentStatus() != null) {
                throw new IllegalStateException("Delivery already exists");
            }
            delivery.addStatusHistory(DeliveryStatus.NEW, message.getTimestamp());
            deliveryRepository.save(delivery);
            return;
        }

        // 3. Проверяем текущий статус
        if (message.getCurrentStatus() != null && !message.getCurrentStatus().equals(delivery.getCurrentStatus())) {
            log.info("message status : {} ", message.getCurrentStatus().toString());
            log.info("delivery status : {}", delivery.getCurrentStatus().toString());
            throw new IllegalStateException("Current status mismatch");
        }

        // 4. Создаем State Machine с текущим состоянием
        StateMachine<DeliveryStatus, DeliveryStatus> sm = stateMachineBuilder.build(delivery);

        // 5. Пытаемся выполнить переход
        if (!sm.sendEvent(message.getToStatus())) {
            throw new IllegalStateException("Invalid status transition: " +
                    delivery.getCurrentStatus() + " → " + message.getToStatus());
        }

        // 6. Сохраняем новый статус
        delivery.addStatusHistory(sm.getState().getId(), message.getTimestamp());
    }


}