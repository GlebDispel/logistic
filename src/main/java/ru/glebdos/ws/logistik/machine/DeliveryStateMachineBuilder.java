package ru.glebdos.ws.logistik.machine;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;
import ru.glebdos.ws.logistik.data.entity.postgresql.Delivery;
import ru.glebdos.ws.logistik.data.entity.postgresql.DeliveryStatus;

@Component
public class DeliveryStateMachineBuilder {
    private final StateMachineFactory<DeliveryStatus, DeliveryStatus> factory;

    public DeliveryStateMachineBuilder(StateMachineFactory<DeliveryStatus, DeliveryStatus> factory) {
        this.factory = factory;
    }

    public StateMachine<DeliveryStatus, DeliveryStatus> build(Delivery delivery) {
        StateMachine<DeliveryStatus, DeliveryStatus> sm = factory.getStateMachine();
        sm.getStateMachineAccessor().doWithAllRegions(access ->
                access.resetStateMachine(new DefaultStateMachineContext<>(
                        delivery.getCurrentStatus(), null, null, null)));
        sm.start();
        return sm;
    }
}