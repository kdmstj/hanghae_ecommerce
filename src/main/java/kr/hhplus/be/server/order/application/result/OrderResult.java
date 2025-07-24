package kr.hhplus.be.server.order.application.result;

import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;

import java.util.List;

public record OrderResult(
        Order order,
        List<OrderProduct> products,
        OrderPayment payment,
        List<UserCoupon> coupons
) {}
