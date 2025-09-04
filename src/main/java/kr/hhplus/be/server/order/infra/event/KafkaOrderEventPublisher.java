package kr.hhplus.be.server.order.infra.event;

import kr.hhplus.be.server.order.application.event.OrderEventPublisher;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.domain.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private static final Map<Class<?>, String> TOPIC_MAP = Map.of(
            OrderCreatedEvent.class, "order.created"
    );

    @Override
    public void publish(OrderEvent event) {
        String topic = TOPIC_MAP.get(event.getClass());
        kafkaTemplate.send(topic, event);
        log.info("Kafka 메시지 전송 완료. topic={}, event={}", topic, event);
    }
}