package kr.hhplus.be.server.order.application.command;

public record PaymentCreateCommand(
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
}
