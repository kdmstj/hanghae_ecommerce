package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.repository.ProductDaySalesRepository;
import kr.hhplus.be.server.product.domain.value.ProductSalesIncrement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Testcontainers
@SpringBootTest
public class ProductSalesRankingServiceIntegrationTest {

    @Autowired
    private ProductSalesRankingService productSalesRankingService;

    @Autowired
    private ProductDaySalesRepository productDaySalesRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @BeforeEach
    public void setUp(){
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Nested
    @DisplayName("productId 별 quantity 증가")
    class IncreaseQuantity {

        @Test
        @DisplayName("성공")
        void productId별_quantity가_증가하고_TOP상품조회가_가능하다() {
            // given
            LocalDateTime now = LocalDateTime.now();
            Long productA = 101L;
            Long productB = 202L;

            // when
            productDaySalesRepository.incrementProductSalesQuantity(now, List.of(
                    new ProductSalesIncrement(productA, 3),
                    new ProductSalesIncrement(productB, 5)
            ));

            productDaySalesRepository.incrementProductSalesQuantity(now, List.of(
                    new ProductSalesIncrement(productA, 3)
            ));

            // then
            List<SalesProductResult> results = productDaySalesRepository.findTopProducts(2, 1);

            assertThat(results).hasSize(2);

            assertThat(results.get(0).productId()).isEqualTo(productA);
            assertThat(results.get(0).quantity()).isEqualTo(6);

            assertThat(results.get(1).productId()).isEqualTo(productB);
            assertThat(results.get(1).quantity()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("최근 3일 동안 판매량 기준 상위 5개 상품 조회")
    class FindTopProducts {

        @Test
        @DisplayName("성공")
        void 최근3일_TOP5_조회() {
            // given
            LocalDateTime now = LocalDateTime.now();

            productDaySalesRepository.incrementProductSalesQuantity(now, List.of(
                    new ProductSalesIncrement(1L, 10),
                    new ProductSalesIncrement(2L, 20)
            ));

            productDaySalesRepository.incrementProductSalesQuantity(now.minusDays(1), List.of(
                    new ProductSalesIncrement(3L, 30),
                    new ProductSalesIncrement(4L, 40)
            ));

            productDaySalesRepository.incrementProductSalesQuantity(now.minusDays(2), List.of(
                    new ProductSalesIncrement(5L, 50),
                    new ProductSalesIncrement(6L, 60)
            ));

            productDaySalesRepository.incrementProductSalesQuantity(now.minusDays(3), List.of(
                    new ProductSalesIncrement(5L, 100),
                    new ProductSalesIncrement(6L, 60)
            ));

            // when
            List<SalesProductResult> results = productSalesRankingService.findTopProducts(5, 3);

            // then
            assertThat(results).hasSize(5);
            assertThat(results)
                    .extracting(SalesProductResult::productId, SalesProductResult::quantity)
                    .containsExactly(
                            tuple(6L, 60),
                            tuple(5L, 50),
                            tuple(4L, 40),
                            tuple(3L, 30),
                            tuple(2L, 20)
                    );
        }
    }
}
