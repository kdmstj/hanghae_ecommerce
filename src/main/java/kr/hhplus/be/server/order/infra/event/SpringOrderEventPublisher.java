package kr.hhplus.be.server.order.infra.event;

import kr.hhplus.be.server.order.application.event.OrderEventPublisher;
import kr.hhplus.be.server.order.domain.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringOrderEventPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
