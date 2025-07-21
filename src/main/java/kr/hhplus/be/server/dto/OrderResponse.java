package kr.hhplus.be.server.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        LocalDateTime createdAt,
        List<OrderProductResponse> products,
        OrderPaymentResponse payment,
        List<OrderCouponResponse> coupons
) {
}
