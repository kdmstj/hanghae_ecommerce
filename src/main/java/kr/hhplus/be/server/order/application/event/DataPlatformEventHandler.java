package kr.hhplus.be.server.order.application.event;

import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class DataPlatformEventHandler {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCreatedEvent event) {
        log.info("[DataPlatformEventHandler] thread={} id={}",
                Thread.currentThread().getName(), Thread.currentThread().getId());
        //외부 MockAPI 호출
    }
}
