package ru.glebdos.ws.logistik.machine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import ru.glebdos.ws.logistik.entityPostgre.DeliveryStatus;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<DeliveryStatus, DeliveryStatus> {

    @Override
    public void configure(StateMachineStateConfigurer<DeliveryStatus, DeliveryStatus> states) throws Exception {
        states
                .withStates()
                .initial(DeliveryStatus.NEW)
                .states(EnumSet.allOf(DeliveryStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DeliveryStatus, DeliveryStatus> transitions) throws Exception {
        transitions
                .withExternal()
                .source(DeliveryStatus.NEW).target(DeliveryStatus.IN_TRANSIT)
                .event(DeliveryStatus.IN_TRANSIT)
                .and()
                .withExternal()
                .source(DeliveryStatus.IN_TRANSIT).target(DeliveryStatus.ARRIVED)
                .event(DeliveryStatus.ARRIVED)
                .and()
                .withExternal()
                .source(DeliveryStatus.ARRIVED).target(DeliveryStatus.DELIVERY)
                .event(DeliveryStatus.DELIVERY)
                .and()
                .withExternal()
                .source(DeliveryStatus.DELIVERY).target(DeliveryStatus.COMPLETE)
                .event(DeliveryStatus.COMPLETE);
    }
}