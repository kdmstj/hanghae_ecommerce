package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserCouponFixture {
    public static UserCoupon withUserIdAndCouponId(long userId, long couponId) {
        return UserCoupon.builder()
                .id(1L)
                .userId(userId)
                .couponId(couponId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusWeeks(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .updatedAt(LocalDateTime.now())
                .build();

    }

    public static UserCoupon withExpiredAt(LocalDateTime expiredAt){
        return UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .issuedAt(LocalDateTime.now())
                .expiredAt(expiredAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserCoupon withUsedAt(LocalDateTime usedAt){
        return UserCoupon.builder()
                .id(1L)
                .userId(1L)
                .couponId(1L)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusWeeks(1))
                .usedAt(usedAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
