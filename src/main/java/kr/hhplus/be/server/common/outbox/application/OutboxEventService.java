package kr.hhplus.be.server.common.outbox.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.outbox.domain.OutboxEvent;
import kr.hhplus.be.server.common.outbox.domain.OutboxStatus;
import kr.hhplus.be.server.common.outbox.domain.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void create(String aggregateType, long aggregateId, String topic, Object payload) {
        String payloadJson = toJson(payload);
        outboxEventRepository.save(OutboxEvent.create(aggregateType, aggregateId, topic, payloadJson));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload JSON serialization failed", e);
        }
    }

    public List<OutboxEvent> findInitByTopic(String topic) {

        return outboxEventRepository.findAllByStatusAndTopic(OutboxStatus.INIT, topic);
    }

    @Transactional
    public void markPublished(OutboxEvent outboxEvent) {
        outboxEvent.markPublished();
    }

    @Transactional
    public void markFailed(OutboxEvent outboxEvent) {
        outboxEvent.markFailed();
    }
}
