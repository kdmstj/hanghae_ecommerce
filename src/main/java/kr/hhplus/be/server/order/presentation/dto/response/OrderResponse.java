package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.application.result.OrderResult;

import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        List<OrderProductResponse> products,
        OrderPaymentResponse payment,
        List<OrderCouponResponse> coupons
) {
    public static OrderResponse from(OrderResult result) {
        return new OrderResponse(
                result.id(),
                result.userId(),
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
