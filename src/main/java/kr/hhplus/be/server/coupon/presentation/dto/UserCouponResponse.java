package kr.hhplus.be.server.coupon.presentation.dto;

import kr.hhplus.be.server.coupon.application.result.UserCouponResult;

import java.time.LocalDateTime;
import java.util.List;

public record UserCouponResponse(
        Long id,
        Long userId,
        Long couponId,
        LocalDateTime issuedAt,
        LocalDateTime expiredAt
) {
    public static UserCouponResponse from(UserCouponResult result) {
        return new UserCouponResponse(
                result.id(),
                result.userId(),
                result.couponId(),
                result.issuedAt(),
                result.expiredAt()
        );
    }

    public static List<UserCouponResponse> from(List<UserCouponResult> resultList){
        return resultList.stream()
                .map(UserCouponResponse::from)
                .toList();
    }
}
