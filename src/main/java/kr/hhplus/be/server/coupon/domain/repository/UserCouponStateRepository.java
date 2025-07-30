package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponStateRepository extends JpaRepository<UserCouponState, Long> {
    UserCouponState finaOneByUserCouponId(Long userCouponId);
}