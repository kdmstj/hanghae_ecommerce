package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponQuantityRepository extends JpaRepository<CouponQuantity, Long> {
    CouponQuantity findOneByCouponId(Long couponId);
}
