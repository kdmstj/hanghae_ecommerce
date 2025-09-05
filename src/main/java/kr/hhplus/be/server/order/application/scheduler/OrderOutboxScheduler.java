package kr.hhplus.be.server.order.application.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.outbox.application.OutboxEventService;
import kr.hhplus.be.server.common.outbox.domain.OutboxEvent;
import kr.hhplus.be.server.order.application.event.OrderEventPublisher;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxScheduler {
    private final OutboxEventService outboxEventService;
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedDelay = 1000)
    public void runOrderCreated() {
        List<OutboxEvent> batch = outboxEventService.findInitByTopic("order.created");

        for (OutboxEvent row : batch) {
            try {
                OrderCreatedEvent event = objectMapper.readValue(row.getPayload(), OrderCreatedEvent.class);

                orderEventPublisher.publish(event);

                outboxEventService.markPublished(row);

                log.info("[OUTBOX] published topic={} key={} outboxId={}",
                        row.getTopic(), row.getAggregateId(), row.getId());

            } catch (Exception ex) {
                outboxEventService.markFailed(row);

                log.error("[OUTBOX] publish failed topic={} key={} outboxId={} err={}",
                        row.getTopic(), row.getAggregateId(), row.getId(), ex.toString());

            }
        }
    }
}
