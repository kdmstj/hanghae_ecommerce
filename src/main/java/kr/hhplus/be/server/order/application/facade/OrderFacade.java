package kr.hhplus.be.server.order.application.facade;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.order.application.command.OrderCreateCommand;
import kr.hhplus.be.server.order.application.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.application.event.OrderCreatedProduct;
import kr.hhplus.be.server.order.application.result.OrderAggregate;
import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.point.application.service.PointService;
import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointService pointService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @DistributedLock(
            keys = {
                    "'lock:user:' + #userId",
                    "#command.products.![ 'lock:product:' + productId ]"
            },
            type = DistributedLock.LockType.MULTI
    )
    public OrderResult place(long userId, OrderCreateCommand command) {
        OrderAggregate orderAggregate = orderService.create(userId, command.products(), command.payment(), command.coupons());
        long orderId = orderAggregate.order().getId();
        LocalDateTime orderedAt = LocalDateTime.now();

        productService.decreaseQuantity(command.products());

        pointService.use(orderId, command.point());

        try {
            couponService.use(command.coupons());
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(ErrorCode.CONFLICT_USE);
        }

        applicationEventPublisher.publishEvent(new OrderCreatedEvent(
                orderedAt,
                command.products().stream()
                        .map(product -> new OrderCreatedProduct(product.productId(), product.quantity()))
                        .toList()
        ));

        return OrderResult.from(orderAggregate);
    }
}
