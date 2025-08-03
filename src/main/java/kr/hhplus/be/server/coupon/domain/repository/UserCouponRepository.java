package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    @Query("""
    SELECT uc
    FROM UserCoupon uc
    JOIN UserCouponState ucs ON uc.id = ucs.userCouponId
    WHERE uc.userId = :userId
      AND ucs.userCouponStatus = 'ISSUED'
    """)
    List<UserCoupon> findAllValidByUserId(@Param("userId") long userId);
    Boolean existsByUserIdAndCouponId(Long userId, Long couponId);
}
