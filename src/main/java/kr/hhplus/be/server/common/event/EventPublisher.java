package kr.hhplus.be.server.common.event;

public interface EventPublisher<E extends Event> {
    void publish(E event);
}
