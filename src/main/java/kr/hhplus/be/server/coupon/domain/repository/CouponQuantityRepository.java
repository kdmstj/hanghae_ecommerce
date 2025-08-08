package kr.hhplus.be.server.coupon.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CouponQuantityRepository extends JpaRepository<CouponQuantity, Long> {
    CouponQuantity findOneByCouponId(Long couponId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cq from CouponQuantity cq where couponId = :couponId")
    CouponQuantity findWithPessimisticLock(long couponId);
}
