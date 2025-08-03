package kr.hhplus.be.server.order.application.result;


import kr.hhplus.be.server.order.domain.entity.Order;

import java.util.List;

public record OrderResult(
        long id,
        long userId,
        List<OrderProductResult> products,
        OrderPaymentResult payment,
        List<OrderCouponResult> coupons
) {
    public static OrderResult from(OrderAggregate orderAggregate){
        Order order = orderAggregate.order();

        return new OrderResult(
                order.getId(),
                order.getUserId(),
                orderAggregate.products().stream().map(OrderProductResult::from).toList(),
                OrderPaymentResult.from(orderAggregate.payment()),
                orderAggregate.coupons().stream().map(OrderCouponResult::from).toList()
        );
    }
}
