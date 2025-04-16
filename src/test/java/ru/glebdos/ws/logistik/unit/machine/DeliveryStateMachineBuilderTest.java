package ru.glebdos.ws.logistik.unit.machine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.config.StateMachineFactory;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;
import ru.glebdos.ws.logistik.machine.DeliveryStateMachineBuilder;

import java.time.Instant;
import java.util.function.Consumer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryStateMachineBuilderTest {

    @Mock
    StateMachineFactory<DeliveryStatus, DeliveryStatus> factory;

    @Mock
    StateMachine<DeliveryStatus, DeliveryStatus> stateMachine;

    @Mock
    StateMachineAccessor<DeliveryStatus, DeliveryStatus> accessor;

    @Mock
    StateMachineAccess<DeliveryStatus, DeliveryStatus> access;

    DeliveryStateMachineBuilder builder;

    @BeforeEach
    void setup() {
        builder = new DeliveryStateMachineBuilder(factory);
    }

    @Test
    void build_ShouldReturnConfiguredStateMachine() {
        // given
        Delivery delivery = new Delivery();
        delivery.addStatusHistory(DeliveryStatus.NEW,Instant.now());

        when(factory.getStateMachine()).thenReturn(stateMachine);
        when(stateMachine.getStateMachineAccessor()).thenReturn(accessor);

        doAnswer(invocation -> {
            Consumer<StateMachineAccess<DeliveryStatus, DeliveryStatus>> consumer = invocation.getArgument(0);
            consumer.accept(access);
            return null;
        }).when(accessor).doWithAllRegions(any());

        // when
        StateMachine<DeliveryStatus, DeliveryStatus> result = builder.build(delivery);

        // then
        assertThat(result).isEqualTo(stateMachine);
        verify(factory).getStateMachine();
        verify(stateMachine).start();
        verify(accessor).doWithAllRegions(any());
        verify(access).resetStateMachine(any());
    }

}