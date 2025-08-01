package kr.hhplus.be.server.order.application.facade;

import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.order.application.command.OrderCreateCommand;
import kr.hhplus.be.server.order.application.result.OrderAggregate;
import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.point.application.service.PointService;
import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final ProductService productService;
    private final CouponService couponService;
    private final PointService pointService;

    @Transactional
    public OrderResult place(long userId, OrderCreateCommand command) {
        OrderAggregate orderAggregate = orderService.create(userId, command.products(), command.payment());
        long orderId = orderAggregate.order().getId();

        productService.decreaseQuantity(command.products());

        List<UserCoupon> orderCoupons = List.of();
        if (command.coupons() != null && !command.coupons().isEmpty()) {
            orderCoupons = couponService.use(orderId, command.coupons());
        }

        pointService.use(orderId, command.point());

        return OrderResult.from(orderAggregate, orderCoupons);
    }
}
