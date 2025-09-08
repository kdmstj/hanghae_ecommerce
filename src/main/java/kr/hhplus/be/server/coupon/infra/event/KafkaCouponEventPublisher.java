package kr.hhplus.be.server.coupon.infra.event;

import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.config.kafka.CouponTopicProperties;
import kr.hhplus.be.server.coupon.application.event.CouponEventPublisher;
import kr.hhplus.be.server.coupon.domain.event.CouponEvent;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaCouponEventPublisher implements CouponEventPublisher {

    private final KafkaTemplate<String, CouponEvent> kafkaTemplate;
    private final CouponTopicProperties couponTopicProperties;

    private Map<Class<?>, String> TOPIC_MAP;

    @PostConstruct
    void init() {
        TOPIC_MAP = new HashMap<>();
        TOPIC_MAP.put(CouponIssueEvent.class, couponTopicProperties.issueName());
    }

    @Override
    public void publish(CouponEvent event) {
        String topic = TOPIC_MAP.get(event.getClass());
        if (event instanceof CouponIssueEvent e) {
            String key = String.valueOf(e.couponId());
            kafkaTemplate.send(topic, key, event);
            log.info("Kafka 메시지 전송 완료. topic={}, key={}, event={}", "coupon.issue", key, event);
        } else {
            log.warn("지원되지 않는 이벤트 타입: {}", event.getClass().getName());
        }
    }
}
