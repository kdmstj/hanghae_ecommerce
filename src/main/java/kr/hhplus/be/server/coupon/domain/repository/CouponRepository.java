package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findAllByIdIn(Set<Long> ids);
}
