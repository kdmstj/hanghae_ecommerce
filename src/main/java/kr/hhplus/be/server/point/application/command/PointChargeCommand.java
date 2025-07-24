package kr.hhplus.be.server.point.application.command;

public record PointChargeCommand(
        long userId,
        int amount
) {

}
