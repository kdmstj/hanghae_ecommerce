package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.application.result.OrderCouponResult;

public record OrderCouponResponse(
        Long userCouponId,
        int discountAmount
) {
    public static OrderCouponResponse from(OrderCouponResult orderCouponResult) {
        return new OrderCouponResponse(
                orderCouponResult.id(),
                orderCouponResult.discountAmount());
    }
}
