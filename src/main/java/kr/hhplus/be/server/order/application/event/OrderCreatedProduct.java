package kr.hhplus.be.server.order.application.event;

public record OrderCreatedProduct(
        long productId,
        int quantity
) {
}
