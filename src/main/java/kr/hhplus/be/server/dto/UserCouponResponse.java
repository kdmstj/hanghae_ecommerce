package kr.hhplus.be.server.dto;

import java.time.LocalDateTime;

public record UserCouponResponse(
        Long userCouponId,
        Long couponId,
        String name,
        String discountType,
        int discountValue,
        LocalDateTime issued_at,
        LocalDateTime expired_at
) {
}
