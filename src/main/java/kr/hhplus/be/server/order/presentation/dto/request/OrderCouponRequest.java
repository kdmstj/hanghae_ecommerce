package kr.hhplus.be.server.order.presentation.dto.request;

import kr.hhplus.be.server.order.application.command.CouponUseCommand;

public record OrderCouponRequest(
        long userCouponId,
        int discountAmount
) {
    public CouponUseCommand toCommand(){
        return new CouponUseCommand(userCouponId, discountAmount);
    }
}
