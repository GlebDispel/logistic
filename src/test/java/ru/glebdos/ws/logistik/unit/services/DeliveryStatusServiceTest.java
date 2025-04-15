package ru.glebdos.ws.logistik.unit.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.repository.postgresql.DeliveryRepository;
import ru.glebdos.ws.logistik.machine.DeliveryStateMachineBuilder;
import ru.glebdos.ws.logistik.services.DeliveryStatusService;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DeliveryStatusServiceTest {


    @InjectMocks
    DeliveryStatusService deliveryStatusService;

    @Mock
    DeliveryRepository deliveryRepository;

    @Mock
    DeliveryStateMachineBuilder deliveryStateMachineBuilder;

    @Test
    @DisplayName("Успешное сохранение нового заказа")
    void processStatusUpdate_WhenNewDelivery_ShouldCreateAndSave(){

        //given
        Instant now = Instant.now();
        long deliveryId = 1L;
        DeliveryStatusMessage message = new DeliveryStatusMessage(deliveryId,null,DeliveryStatus.NEW, now);

        when(deliveryRepository.findWithStatusHistoryById(deliveryId)).thenReturn(Optional.empty());

        ArgumentCaptor<Delivery> captor = ArgumentCaptor.forClass(Delivery.class);

        //when
        deliveryStatusService.processStatusUpdate(message);

        //then
        verify(deliveryRepository).save(captor.capture());

        Delivery saved = captor.getValue();

        assertEquals(deliveryId, saved.getId());
        assertEquals(DeliveryStatus.NEW, saved.getCurrentStatus());
        assertEquals(1, saved.getStatusHistory().size());
        assertEquals(now, saved.getStatusHistory().get(0).getStatusTimestamp());
    }

    @Test
    @DisplayName("Доставка уже существует, но приходит статус NEW - выбрасывает исключение")
    void processStatusUpdate_WhenDeliveryExistWithStatusAndNewStatusCome_ShouldThrowException(){

        //given
        DeliveryStatusMessage message = new DeliveryStatusMessage();
        message.setId(1L);
        message.setToStatus(DeliveryStatus.NEW);
        message.setTimestamp(Instant.now());

        Delivery existing = new Delivery();
        existing.setId(1L);
        existing.addStatusHistory(DeliveryStatus.IN_TRANSIT,Instant.now());

        when(deliveryRepository.findWithStatusHistoryById(1L)).thenReturn(
                Optional.of(existing));

        //when+then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryStatusService.processStatusUpdate(message));

        assertEquals("Delivery already exists",exception.getMessage());
        verify(deliveryRepository,never()).save(any());

    }

    @Test
    @DisplayName("Текущие статусы не совпадают - выбрасывается исключение")
    void processStatusUpdate_WhenDeliveryStatusMismatchMessageStatus_shouldThrowException(){
        DeliveryStatusMessage message = new DeliveryStatusMessage();
        message.setId(1L);
        message.setCurrentStatus(DeliveryStatus.NEW);
        message.setToStatus(DeliveryStatus.IN_TRANSIT);
        message.setTimestamp(Instant.now());

        Delivery existing = new Delivery();
        existing.setId(1L);
        existing.addStatusHistory(DeliveryStatus.IN_TRANSIT,Instant.now());

        when(deliveryRepository.findWithStatusHistoryById(1L))
                .thenReturn(Optional.of(existing));

        //when+then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> deliveryStatusService.processStatusUpdate(message));

        assertEquals("Current status mismatch",exception.getMessage());
        verify(deliveryRepository,never()).save(any());
    }

    @Test
    @DisplayName("Успешное обновление статуса")
    void processStatusUpdate_WhenDeliveryAndMessageStatusValid_ShouldSaveNewStatus(){
        //given
        Instant now = Instant.now();
        long deliveryId = 1L;
        DeliveryStatusMessage message = new DeliveryStatusMessage(deliveryId,DeliveryStatus.NEW,DeliveryStatus.IN_TRANSIT,now);

        Delivery existing = new Delivery();
        existing.setId(1L);
        existing.addStatusHistory(DeliveryStatus.NEW,now.minusSeconds(60));

        when(deliveryRepository.findWithStatusHistoryById(deliveryId))
                .thenReturn(Optional.of(existing));





        // Мокаем stateMachine
        StateMachine<DeliveryStatus, DeliveryStatus> mockStateMachine = mock(StateMachine.class);
        when(mockStateMachine.sendEvent(DeliveryStatus.IN_TRANSIT)).thenReturn(true);

        State<DeliveryStatus, DeliveryStatus> mockState = mock(State.class);
        when(mockState.getId()).thenReturn(DeliveryStatus.IN_TRANSIT);
        when(mockStateMachine.getState()).thenReturn(mockState);

        when(deliveryStateMachineBuilder.build(existing)).thenReturn(mockStateMachine);


        //when
        deliveryStatusService.processStatusUpdate(message);

        //then
        assertEquals(DeliveryStatus.IN_TRANSIT, existing.getCurrentStatus());
        assertEquals(2, existing.getStatusHistory().size());
        assertEquals(now, existing.getStatusHistory().get(1).getStatusTimestamp());


    }
}