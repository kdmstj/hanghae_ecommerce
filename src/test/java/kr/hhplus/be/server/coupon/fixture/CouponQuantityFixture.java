package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;

import java.time.LocalDateTime;

public class CouponQuantityFixture {
    public static CouponQuantity withTotalQuantityAndIssuedQuantity(int totalQuantity, int issuedQuantity){
        return CouponQuantity.builder()
                .couponId(1L)
                .totalQuantity(totalQuantity)
                .issuedQuantity(issuedQuantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CouponQuantity withCouponId(long couponId) {
        return CouponQuantity.builder()
                .couponId(couponId)
                .totalQuantity(100)
                .issuedQuantity(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CouponQuantity withCouponIdAndTotalQuantityAndIssuedQuantity(long couponId, int totalQuantity, int issuedQuantity){
        return CouponQuantity.builder()
                .couponId(couponId)
                .totalQuantity(totalQuantity)
                .issuedQuantity(issuedQuantity)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
