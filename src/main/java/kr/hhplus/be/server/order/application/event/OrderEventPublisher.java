package kr.hhplus.be.server.order.application.event;

import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.order.domain.event.OrderEvent;

public interface OrderEventPublisher extends EventPublisher<OrderEvent> {
}
