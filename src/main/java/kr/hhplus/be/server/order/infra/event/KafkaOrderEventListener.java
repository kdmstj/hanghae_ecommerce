package kr.hhplus.be.server.order.infra.event;

import kr.hhplus.be.server.order.application.event.OrderEventListener;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.product.application.command.SalesProductCommand;
import kr.hhplus.be.server.product.application.service.ProductSalesRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderEventListener implements OrderEventListener {

    private final ProductSalesRankingService productSalesRankingService;

    @KafkaListener(
            topics = "order.created",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void on(OrderCreatedEvent event) {

        log.info("주문 완료 이벤트 리스너 성공");
        log.info("[DataPlatformEventHandler] thread={} id={}",
                Thread.currentThread().getName(), Thread.currentThread().getId());

        log.info("[ProductSalesRankingEventHandler] thread={} id={}",
                Thread.currentThread().getName(), Thread.currentThread().getId());

        productSalesRankingService.increaseQuantity(
                event.orderedAt(),
                event.products().stream()
                        .map(product -> new SalesProductCommand(product.productId(), product.quantity()))
                        .toList()
        );
    }
}
