package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;

import java.time.LocalDateTime;

public class UserCouponStateFixture {
    public static UserCouponState withUserCouponStatus(UserCouponStatus userCouponStatus) {
        return UserCouponState.builder()
                .id(1L)
                .userCouponId(1L)
                .userCouponStatus(userCouponStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserCouponState withUserCouponIdAndWithUserCouponStatus(
            long userCouponId, UserCouponStatus userCouponStatus
    ) {
        return UserCouponState.builder()
                .userCouponId(userCouponId)
                .userCouponStatus(userCouponStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
