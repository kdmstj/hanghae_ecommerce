package kr.hhplus.be.server.product.application.scheduler;

import kr.hhplus.be.server.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BestProductAggregateScheduler {

    private final ProductService productService;

    @Scheduled(cron = "0 10 * * * *", zone = "Asia/Seoul")
    public void run() {
        productService.refreshTop5BestProductsFor3Days();
    }

}
