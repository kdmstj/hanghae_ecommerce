package kr.hhplus.be.server.coupon.domain.event;

public record CouponIssueEvent(
        long userId,
        long couponId
) implements CouponEvent {
}
