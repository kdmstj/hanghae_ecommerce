package kr.hhplus.be.server.coupon.presentation.dto;

import kr.hhplus.be.server.coupon.domain.DiscountType;

import java.time.LocalDateTime;

public record CouponResponse(
        long id,
        String name,
        DiscountType discountType,
        int discountValue,
        int issuedQuantity,
        LocalDateTime issuedStartedAt,
        LocalDateTime issuedEndedAt
) {
}
