package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.application.result.OrderProductResult;

public record OrderProductResponse(
        long id,
        long productId,
        int quantity
) {
    public static OrderProductResponse from(OrderProductResult orderProductResult) {
        return new OrderProductResponse(
                orderProductResult.id(),
                orderProductResult.productId(),
                orderProductResult.quantity()
        );
    }
}
