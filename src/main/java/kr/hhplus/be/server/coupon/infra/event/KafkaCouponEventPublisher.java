package kr.hhplus.be.server.coupon.infra.event;

import kr.hhplus.be.server.coupon.application.event.CouponEventPublisher;
import kr.hhplus.be.server.coupon.domain.event.CouponEvent;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCouponEventPublisher implements CouponEventPublisher {

    private final KafkaTemplate<String, CouponEvent> kafkaTemplate;

    private static final Map<Class<?>, String> TOPIC_MAP = Map.of(
           CouponIssueEvent.class, "coupon.issue"
    );

    @Override
    public void publish(CouponEvent event) {
        if (event instanceof CouponIssueEvent e) {
            String key = String.valueOf(e.couponId());
            kafkaTemplate.send("coupon.issue", key, event);
            log.info("Kafka 메시지 전송 완료. topic={}, key={}, event={}", "coupon.issue", key, event);
        } else {
            log.warn("지원되지 않는 이벤트 타입: {}", event.getClass().getName());
        }
    }
}
