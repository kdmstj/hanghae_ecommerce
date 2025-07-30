package kr.hhplus.be.server.point.application.command;

public record PointUseCommand(
        long userId,
        int amount
) {
}
