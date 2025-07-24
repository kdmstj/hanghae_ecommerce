package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    List<UserCoupon> findAllByUserIdAndUsedAtIsNullAndExpiredAtAfter(Long userId, LocalDateTime now);
    Boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
