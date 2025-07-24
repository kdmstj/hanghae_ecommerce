package kr.hhplus.be.server.order.presentation.dto.request;

import kr.hhplus.be.server.order.application.command.ProductDecreaseCommand;

public record OrderProductRequest(
        long productId,
        int quantity
) {
    public ProductDecreaseCommand toCommand(){
        return new ProductDecreaseCommand(productId, quantity);
    }
}
