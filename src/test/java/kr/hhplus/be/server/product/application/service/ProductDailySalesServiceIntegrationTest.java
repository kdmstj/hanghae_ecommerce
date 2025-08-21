package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.product.domain.entity.ProductDailySales;
import kr.hhplus.be.server.product.domain.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.product.domain.repository.ProductDaySalesRepository;
import kr.hhplus.be.server.product.domain.value.ProductSalesIncrement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Testcontainers
@SpringBootTest
public class ProductDailySalesServiceIntegrationTest {

    @Autowired
    private ProductDailySalesService productDailySalesService;

    @Autowired
    private ProductDaySalesRepository productDaySalesRepository;

    @Autowired
    private ProductDailySalesRepository productDailySalesRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @Autowired
    private RedisTemplate redisTemplate;

    @BeforeEach
    void setUp(){
        dataBaseCleanUp.execute();
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Test
    @DisplayName("Redis에 집계된 판매량이 DB product_daily_sales 테이블에 저장된다")
    void create_메서드가_집계데이터를_DB에_저장한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Long productA = 1001L;
        Long productB = 1002L;

        productDaySalesRepository.incrementProductSalesQuantity(now.minusDays(1), List.of(
                new ProductSalesIncrement(productA, 5),
                new ProductSalesIncrement(productB, 3)
        ));

        // when
        productDailySalesService.create();

        // then
        List<ProductDailySales> saved = productDailySalesRepository.findAll();

        assertThat(saved).hasSize(2);
        assertThat(saved)
                .extracting(ProductDailySales::getProductId, ProductDailySales::getSalesDate, ProductDailySales::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(productA, LocalDate.now().minusDays(1), 5),
                        tuple(productB, LocalDate.now().minusDays(1), 3)
                );
    }

}
