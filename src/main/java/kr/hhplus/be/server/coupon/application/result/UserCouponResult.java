package kr.hhplus.be.server.coupon.application.result;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

import java.time.LocalDateTime;

public record UserCouponResult(
        long id,
        long userId,
        long couponId,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
) {
    public static UserCouponResult from(UserCoupon userCoupon) {
        return new UserCouponResult(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt()
        );
    }
}

