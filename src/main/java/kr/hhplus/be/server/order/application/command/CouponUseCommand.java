package kr.hhplus.be.server.order.application.command;

public record CouponUseCommand(
        long userCouponId,
        int discountAmount
) {
}
