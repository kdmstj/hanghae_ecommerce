package kr.hhplus.be.server.order.infra;

import kr.hhplus.be.server.order.application.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.application.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventApplicationPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCreatedEvent orderCreatedEvent) {
        applicationEventPublisher.publishEvent(orderCreatedEvent);
    }
}
