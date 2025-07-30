package kr.hhplus.be.server.order.application.result;

import kr.hhplus.be.server.order.domain.entity.OrderPayment;

public record OrderPaymentResult(
        Long id,
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
    public static OrderPaymentResult from(OrderPayment orderPayment){
        return new OrderPaymentResult(
                orderPayment.getId(),
                orderPayment.getOrderAmount(),
                orderPayment.getDiscountAmount(),
                orderPayment.getPaymentAmount()
        );
    }
}
