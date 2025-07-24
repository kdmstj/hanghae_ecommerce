package kr.hhplus.be.server.order.presentation.dto.response;

import kr.hhplus.be.server.order.domain.entity.OrderProduct;

public record OrderProductResponse(
        long id,
        long productId,
        int quantity
) {
    public static OrderProductResponse from(OrderProduct op) {
        return new OrderProductResponse(op.getId(), op.getProductId(), op.getQuantity());
    }
}
