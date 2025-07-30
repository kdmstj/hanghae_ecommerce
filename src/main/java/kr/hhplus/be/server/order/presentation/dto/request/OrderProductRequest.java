package kr.hhplus.be.server.order.presentation.dto.request;

import kr.hhplus.be.server.order.application.command.OrderProductCommand;

public record OrderProductRequest(
        long productId,
        int quantity
) {
    public OrderProductCommand toCommand(){
        return new OrderProductCommand(productId, quantity);
    }
}
