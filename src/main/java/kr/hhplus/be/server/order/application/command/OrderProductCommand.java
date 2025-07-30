package kr.hhplus.be.server.order.application.command;

public record OrderProductCommand(
        long productId,
        int quantity
) {
}
