package kr.hhplus.be.server.order.application.command;

public record ProductDecreaseCommand(
        long productId,
        int quantity
) {
}
