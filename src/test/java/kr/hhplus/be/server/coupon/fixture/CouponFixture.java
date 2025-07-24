package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.DiscountType;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;

import java.time.LocalDateTime;

public class CouponFixture {

    public static Coupon withTotalQuantityAndIssuedQuantity(int totalQuantity, int issuedQuantity) {
        return Coupon.builder()
                .id(1L)
                .name("coupon")
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .totalQuantity(totalQuantity)
                .issuedQuantity(issuedQuantity)
                .issuedStartedAt(LocalDateTime.now().minusWeeks(1))
                .issuedEndedAt(LocalDateTime.now().plusWeeks(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Coupon withIssuedQuantityAndIssuedStartedAtAndIssuedEndedAt(int issuedQuantity, LocalDateTime issuedStartedAt, LocalDateTime issuedEndedAt){
        return Coupon.builder()
                .id(1L)
                .name("coupon")
                .discountType(DiscountType.FIXED)
                .discountValue(10000)
                .totalQuantity(10)
                .issuedQuantity(issuedQuantity)
                .issuedStartedAt(issuedStartedAt)
                .issuedEndedAt(issuedEndedAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
