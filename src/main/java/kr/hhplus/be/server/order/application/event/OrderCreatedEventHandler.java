package kr.hhplus.be.server.order.application.event;

import kr.hhplus.be.server.product.application.command.SalesProductCommand;
import kr.hhplus.be.server.product.application.service.ProductSalesRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventHandler {

    private final ProductSalesRankingService productSalesRankingService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent Handler 실행");
        productSalesRankingService.increaseQuantity(
                event.orderedAt(),
                event.products().stream()
                        .map(product -> new SalesProductCommand(product.productId(), product.quantity()))
                        .toList()
        );
    }
}
