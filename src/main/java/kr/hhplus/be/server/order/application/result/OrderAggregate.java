package kr.hhplus.be.server.order.application.result;

import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderCoupon;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;

import java.util.List;

public record OrderAggregate(
        Order order,
        List<OrderProduct> products,
        OrderPayment payment,
        List<OrderCoupon> coupons
) {
}
