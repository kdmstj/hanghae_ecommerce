package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.domain.entity.OrderPayment;

public record OrderPaymentResponse(
        Long id,
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
    public static OrderPaymentResponse from(OrderPayment op) {
        return new OrderPaymentResponse(op.getId(), op.getOrderAmount(), op.getDiscountAmount(), op.getPaymentAmount());
    }
}
