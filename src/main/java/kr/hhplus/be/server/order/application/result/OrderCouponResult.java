package kr.hhplus.be.server.order.application.result;

import kr.hhplus.be.server.order.domain.entity.OrderCoupon;

public record OrderCouponResult(
        long id,
        long userCouponId,
        int discountAmount
) {
    public static OrderCouponResult from(OrderCoupon orderCoupon){
        return new OrderCouponResult(
                orderCoupon.getId(),
                orderCoupon.getUserCouponId(),
                orderCoupon.getDiscountAmount()
        );
    }
}
