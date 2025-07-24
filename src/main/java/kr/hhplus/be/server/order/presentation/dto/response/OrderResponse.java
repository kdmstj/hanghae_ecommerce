package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;

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
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.order().getId(),
                result.order().getUserId(),
                result.order().getCreatedAt(),
                result.products().stream()
                        .map(OrderProductResponse::from)
                        .toList(),
                OrderPaymentResponse.from(result.payment()),
                result.coupons().stream()
                        .map(OrderCouponResponse::from)
                        .toList()
        );
    }
}
