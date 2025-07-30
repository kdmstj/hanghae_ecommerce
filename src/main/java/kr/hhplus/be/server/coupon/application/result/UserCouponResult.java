package kr.hhplus.be.server.coupon.application.result;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResult(
        long id,
        long userId,
        long couponId,
        long orderId,
        int discountAmount,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt,
        LocalDateTime usedAt
) {
    public static UserCouponResult from(UserCoupon userCoupon) {
        return new UserCouponResult(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                userCoupon.getOrderId(),
                userCoupon.getDiscountAmount(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt(),
                userCoupon.getUsedAt()
        );
    }
}

