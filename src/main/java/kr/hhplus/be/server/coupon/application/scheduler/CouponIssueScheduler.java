package kr.hhplus.be.server.coupon.application.scheduler;

import kr.hhplus.be.server.coupon.application.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponIssueScheduler {

    private final CouponService couponService;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedDelay = 1000)
    public void processIssueQueue() {
        couponService.issuePendingCoupons(BATCH_SIZE);
    }
}
