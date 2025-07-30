package kr.hhplus.be.server.coupon.fixture;

import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;

import java.time.LocalDateTime;

public class UserCouponStateFixture {
    public static UserCouponState withUserCouponStatus(UserCouponStatus userCouponStatus){
        return new UserCouponState(1, 1, userCouponStatus, LocalDateTime.now(), LocalDateTime.now());
    }
}
