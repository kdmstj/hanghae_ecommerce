package kr.hhplus.be.server.coupon.presentation.dto;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

import java.time.LocalDateTime;
import java.util.List;

public record UserCouponResponse(
        Long id,
        Long userId,
        Long couponId,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
) {
    public static UserCouponResponse from(UserCoupon userCoupon) {
        return new UserCouponResponse(
                userCoupon.getId(),
                userCoupon.getUserId(),
                userCoupon.getCouponId(),
                userCoupon.getIssuedAt(),
                userCoupon.getExpiredAt()
        );
    }

    public static List<UserCouponResponse> from(List<UserCoupon> userCoupons){
        return userCoupons.stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}
