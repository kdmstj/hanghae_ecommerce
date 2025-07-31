package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCouponStateRepository extends JpaRepository<UserCouponState, Long> {
    Optional<UserCouponState> findOneByUserCouponId(Long userCouponId);
}