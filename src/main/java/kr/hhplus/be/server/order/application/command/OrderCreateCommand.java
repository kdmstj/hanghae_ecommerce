package kr.hhplus.be.server.order.application.command;

import kr.hhplus.be.server.point.application.command.PointUseCommand;

import java.util.List;

public record OrderCreateCommand(
        PaymentCreateCommand payment,
        List<ProductDecreaseCommand> products,
        List<CouponUseCommand> coupons,
        PointUseCommand point
) {
}
