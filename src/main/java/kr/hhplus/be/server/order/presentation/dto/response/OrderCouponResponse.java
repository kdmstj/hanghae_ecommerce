package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.coupon.application.result.UserCouponResult;

public record OrderCouponResponse(
        Long userCouponId,
        int discountAmount
) {
    public static OrderCouponResponse from(UserCouponResult userCouponResult) {
        return new OrderCouponResponse(
                userCouponResult.id(),
                userCouponResult.discountAmount());
    }
}
