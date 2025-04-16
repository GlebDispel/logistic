package ru.glebdos.ws.logistik.unit.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatusMessage;
import ru.glebdos.ws.logistik.data.repository.postgresql.FailedEventRepository;
import ru.glebdos.ws.logistik.services.DeliveryStatusService;
import ru.glebdos.ws.logistik.util.RetryManager;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RetryManagerTest {


    @InjectMocks
    RetryManager retryManager;

    @Mock
    ScheduledExecutorService scheduler;

    @Mock
    DeliveryStatusService deliveryStatusService;

    @Mock
    FailedEventRepository failedEventRepository;

    @Captor
    ArgumentCaptor<Runnable> runnableCaptor;

    @Test
    @DisplayName("Успешная обработка с первого ретрая")
    void scheduleRetry_ShouldCallProcessStatusUpdateOnce_OnSuccess() {
        // given
        DeliveryStatusMessage message = new DeliveryStatusMessage(1L, null, DeliveryStatus.NEW, Instant.now());
        ConsumerRecord<String, DeliveryStatusMessage> record = new ConsumerRecord<>("topic", 0, 0L, "key", message);

        // when
        retryManager.scheduleRetry(record, 1);

        // then
        verify(scheduler).schedule(runnableCaptor.capture(), eq(10L), eq(TimeUnit.SECONDS));

        // simulate run
        runnableCaptor.getValue().run();

        verify(deliveryStatusService).processStatusUpdate(message);
        verifyNoInteractions(failedEventRepository);
    }

    @Test
    @DisplayName("Успешная обработка со второго ретрая")
    void scheduleRetry_WhenFirstFailure_ShouldSuccessSecondRetry(){
        // given
        DeliveryStatusMessage message = new DeliveryStatusMessage(1L, null, DeliveryStatus.NEW, Instant.now());
        ConsumerRecord<String, DeliveryStatusMessage> record = new ConsumerRecord<>("topic", 0, 0L, "key", message);

        doThrow(new RuntimeException("First fail"))
                .doNothing()
                .when(deliveryStatusService).processStatusUpdate(message);

        //when
        retryManager.scheduleRetry(record,1);

        //then
        verify(scheduler).schedule(runnableCaptor.capture(),eq(10L), eq(TimeUnit.SECONDS));

        Runnable firstAttemptRunnable = runnableCaptor.getValue();

        firstAttemptRunnable.run();

        verify(scheduler)
                .schedule(runnableCaptor.capture(),eq(30L),eq(TimeUnit.SECONDS));

        Runnable secondAttemptRunnable = runnableCaptor.getValue();

        secondAttemptRunnable.run();

        verify(deliveryStatusService, times(2)).processStatusUpdate(message);
        verifyNoInteractions(failedEventRepository);
    }

    @Test
    @DisplayName("Неудачные попытки ретрая - запись в failEvent")
    void scheduleRetry_WhenAllRetriesFail_ShouldSaveFailEvent(){
        // given
        Instant now = Instant.now();
        DeliveryStatusMessage message = new DeliveryStatusMessage(1L, null, DeliveryStatus.NEW, now);
        ConsumerRecord<String, DeliveryStatusMessage> record = new ConsumerRecord<>("topic", 0, 0L, "key", message);

        doThrow(new RuntimeException("fail"))
                .when(deliveryStatusService).processStatusUpdate(message);


        //when
        retryManager.scheduleRetry(record,1);

        //then
        verify(scheduler).schedule(runnableCaptor.capture(),eq(10L), eq(TimeUnit.SECONDS));

        Runnable firstAttemptRunnable = runnableCaptor.getValue();

        firstAttemptRunnable.run();

        verify(scheduler)
                .schedule(runnableCaptor.capture(),eq(30L),eq(TimeUnit.SECONDS));

        Runnable secondAttemptRunnable = runnableCaptor.getValue();

        secondAttemptRunnable.run();

        verify(scheduler)
                .schedule(runnableCaptor.capture(),eq(60L),eq(TimeUnit.SECONDS));

        Runnable thirdAttemptRunnable = runnableCaptor.getValue();

        thirdAttemptRunnable.run();

        verify(deliveryStatusService,times(3)).processStatusUpdate(message);
        verify(failedEventRepository).save(argThat(event ->
                event.getDeliveryId() == 1L &&
                event.getCurrentStatus() == null &&
                event.getToStatus() == DeliveryStatus.NEW &&
                event.getEventTimestamp().equals(now) &&
                event.getFailureTimestamp() != null &&
                event.getException().contains("fail")));

    }


}