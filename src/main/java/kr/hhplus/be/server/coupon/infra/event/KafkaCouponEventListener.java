package kr.hhplus.be.server.coupon.infra.event;

import kr.hhplus.be.server.coupon.application.event.CouponEventListener;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCouponEventListener implements CouponEventListener {

    private final CouponService couponService;

    @KafkaListener(
            topics = "coupon.issue",
            groupId = "${spring.kafka.consumer.group-id}",
            concurrency = "3"
    )
    public void on(CouponIssueEvent event){
        try{
            couponService.handleCouponIssue(event.userId(), event.couponId());
            log.info("선착순 쿠폰 성공");
        } catch (Exception e){
            log.error("쿠폰 발급 실패");
        }

    }

}
