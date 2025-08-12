package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class ProductQuantityConcurrencyTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    void setUp() {
        dataBaseCleanUp.execute();
    }

    @DisplayName("재고 차감이 동시에 들어왔을 때")
    @ParameterizedTest
    @CsvSource({
            "10, 10",
            "20, 10"
    })
    void 동시에_상품을_1개씩_구매하면_재고는_요청한만큼_차감되어야_한다(int originQuantity, int threadCount) throws Exception {
        //given
        Product product1 = productRepository.save(ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품1", 1000, originQuantity));

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseQuantity(
                            List.of(new OrderProductCommand(product1.getId(), 1))
                    );
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product result = productRepository.findById(product1.getId()).orElseThrow();
        assertThat(result.getQuantity()).isEqualTo(originQuantity - threadCount);
    }
}
