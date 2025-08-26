package kr.hhplus.be.server.order.application.event;

public interface OrderEventPublisher {
    void publish(OrderCreatedEvent orderCreatedEvent);
}
