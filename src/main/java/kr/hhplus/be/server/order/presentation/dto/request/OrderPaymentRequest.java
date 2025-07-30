package kr.hhplus.be.server.order.presentation.dto.request;

import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;

public record OrderPaymentRequest(
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
    public PaymentCreateCommand toCommand(){
        return new PaymentCreateCommand(orderAmount, discountAmount, paymentAmount);
    }
}
