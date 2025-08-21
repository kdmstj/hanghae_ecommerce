package kr.hhplus.be.server.product.application.command;

public record SalesProductCommand(
        long productId,
        int quantity
) {
}
