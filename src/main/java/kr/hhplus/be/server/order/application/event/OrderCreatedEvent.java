package kr.hhplus.be.server.order.application.event;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
        LocalDateTime orderedAt,
        List<OrderCreatedProduct> products
) {
}
