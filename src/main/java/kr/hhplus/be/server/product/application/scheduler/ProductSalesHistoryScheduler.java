package kr.hhplus.be.server.product.application.scheduler;

import kr.hhplus.be.server.product.application.service.ProductDailySalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSalesHistoryScheduler {

    private final ProductDailySalesService productDailySalesService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    public void snapshotYesterday() {
        productDailySalesService.create();
    }
}
