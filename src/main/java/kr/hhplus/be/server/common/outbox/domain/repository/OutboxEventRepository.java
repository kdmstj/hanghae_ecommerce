package kr.hhplus.be.server.common.outbox.domain.repository;

import kr.hhplus.be.server.common.outbox.domain.OutboxEvent;
import kr.hhplus.be.server.common.outbox.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByStatusAndTopic(OutboxStatus status, String topic);
}
