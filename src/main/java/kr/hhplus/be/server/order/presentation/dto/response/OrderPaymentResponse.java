package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.application.result.OrderPaymentResult;

public record OrderPaymentResponse(
        Long id,
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
    public static OrderPaymentResponse from(OrderPaymentResult orderPaymentResult) {
        return new OrderPaymentResponse(
                orderPaymentResult.id(),
                orderPaymentResult.orderAmount(),
                orderPaymentResult.discountAmount(),
                orderPaymentResult.paymentAmount()
        );
    }
}
