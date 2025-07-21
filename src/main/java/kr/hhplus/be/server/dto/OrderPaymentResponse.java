package kr.hhplus.be.server.dto;

public record OrderPaymentResponse(
        Long id,
        int orderAmount,
        int discountAmount,
        int paymentAmount
) {
}
