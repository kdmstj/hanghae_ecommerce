package kr.hhplus.be.server.coupon.application.event;

import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.coupon.domain.event.CouponEvent;

public interface CouponEventPublisher extends EventPublisher<CouponEvent> {
}
