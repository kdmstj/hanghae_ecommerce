package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserCouponFixture {
    public static UserCoupon withUserIdAndCouponId(long userId, long couponId) {
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusWeeks(1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UserCoupon withIdAndUserIdAndCouponId(long id, long userId, long couponId) {
        return UserCoupon.builder()
                .id(id)
                .userId(userId)
                .couponId(couponId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusWeeks(1))
                .createdAt(LocalDateTime.now())
                .build();

    }

    public static List<UserCoupon> createListWithUserId(int n, long userId) {
        List<UserCoupon> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            UserCoupon userCoupon = withIdAndUserIdAndCouponId(i, userId, i);
            list.add(userCoupon);
        }

        return list;
    }
}
