package kr.hhplus.be.server.order.infra.event;

import jakarta.annotation.PostConstruct;
import kr.hhplus.be.server.config.kafka.OrderTopicProperties;
import kr.hhplus.be.server.order.application.event.OrderEventPublisher;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.domain.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final OrderTopicProperties orderTopicProperties;
    private Map<Class<?>, String> TOPIC_MAP;

    @PostConstruct
    void init() {
        TOPIC_MAP = new HashMap<>();
        TOPIC_MAP.put(OrderCreatedEvent.class, orderTopicProperties.createdName());
    }

    @Override
    public void publish(OrderEvent event) {
        String topic = TOPIC_MAP.get(event.getClass());
        kafkaTemplate.send(topic, event);
        log.info("Kafka 메시지 전송 완료. topic={}, event={}", topic, event);
    }
}