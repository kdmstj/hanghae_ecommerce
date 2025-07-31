package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.DiscountType;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;

import java.time.LocalDateTime;

public class CouponFixture {

    public static Coupon withIssuedStartedAtAndIssuedEndedAt(LocalDateTime issuedStartedAt, LocalDateTime issuedEndedAt) {
        return Coupon.builder()
                .name("coupon")
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .issuedStartedAt(issuedStartedAt)
                .issuedEndedAt(issuedEndedAt)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
