package kr.hhplus.be.server.order.application.result;

import kr.hhplus.be.server.order.domain.entity.OrderProduct;


public record OrderProductResult(
        long id,
        long productId,
        int quantity
) {
    public static OrderProductResult from(OrderProduct orderProduct){
        return new OrderProductResult(
                orderProduct.getId(),
                orderProduct.getProductId(),
                orderProduct.getQuantity()
        );
    }
}
