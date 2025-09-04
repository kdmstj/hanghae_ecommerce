package kr.hhplus.be.server.order.domain.event;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        LocalDateTime orderedAt,
        List<OrderProduct> products
) implements OrderEvent {
    public record OrderProduct(
            long productId,
            int quantity
    ) { }
}
