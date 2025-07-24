package kr.hhplus.be.server.order.application.facade;

import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.order.application.command.OrderCreateCommand;
import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.application.service.OrderPaymentService;
import kr.hhplus.be.server.order.application.service.OrderProductService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
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
    private final OrderProductService orderProductService;
    private final CouponService couponService;
    private final PointService pointService;
    private final OrderPaymentService orderPaymentService;

    @Transactional
    public OrderResult place(long userId, OrderCreateCommand command) {
        Order order = orderService.create(userId);
        long orderId = order.getId();

        productService.reserve(command.products());

        List<OrderProduct> orderProducts = orderProductService.create(orderId, command.products());

        List<UserCoupon> orderCoupons = couponService.use(orderId, command.coupons());

        pointService.use(orderId, command.point());

        OrderPayment orderPayment = orderPaymentService.create(orderId, command.payment());

        return new OrderResult(order, orderProducts, orderPayment, orderCoupons);
    }
}
