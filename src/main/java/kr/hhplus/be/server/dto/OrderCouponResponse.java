package kr.hhplus.be.server.dto;

public record OrderCouponResponse(
        Long id,
        Long userCouponId,
        int discountAmount
) {
}
