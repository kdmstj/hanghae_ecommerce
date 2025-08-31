package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import kr.hhplus.be.server.order.domain.entity.OrderCoupon;
import kr.hhplus.be.server.order.domain.repository.OrderCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCouponService {

    private final OrderCouponRepository orderCouponRepository;

    public List<OrderCoupon> create(long orderId, List<CouponUseCommand> commands) {

        List<OrderCoupon> orderCoupons = commands.stream()
                .map(command -> OrderCoupon.create(
                        orderId,
                        command.userCouponId(),
                        command.discountAmount()
                ))
                .toList();

        return orderCouponRepository.saveAll(orderCoupons);
    }
}
