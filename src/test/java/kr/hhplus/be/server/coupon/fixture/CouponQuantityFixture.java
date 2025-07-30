package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;

import java.time.LocalDateTime;

public class CouponQuantityFixture {
    public static CouponQuantity withTotalQuantityAndIssuedQuantity(int totalQuantity, int issuedQuantity){
        return new CouponQuantity(1L, 1L, totalQuantity, issuedQuantity, LocalDateTime.now(), LocalDateTime.now());
    }
}
