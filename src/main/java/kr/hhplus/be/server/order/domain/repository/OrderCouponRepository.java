package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.entity.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {
}
