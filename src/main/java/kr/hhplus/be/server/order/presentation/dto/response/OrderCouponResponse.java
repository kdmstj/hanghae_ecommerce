package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

public record OrderCouponResponse(
        Long userCouponId,
        int discountAmount
) {
    public static OrderCouponResponse from(UserCoupon userCoupon){
        return new OrderCouponResponse(userCoupon.getId(), userCoupon.getDiscountAmount());
    }
}
