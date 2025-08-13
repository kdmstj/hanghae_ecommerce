package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.application.result.BestProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.entity.ProductDailySales;
import kr.hhplus.be.server.product.domain.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDailySalesRepository productDailySalesRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseQuantity {

        @ParameterizedTest
        @CsvSource({
                "10, 10",
                "10, 9"
        })
        @DisplayName("재고 차감 완료")
        void 완료(int originQuantity, int orderQuantity) {
            //given
            Product product1 = productRepository.save(ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품1", 1000, originQuantity));

            List<OrderProductCommand> commands = List.of(
                    new OrderProductCommand(product1.getId(), orderQuantity)
            );

            // when
            productService.decreaseQuantity(commands);

            // then
            Product updated1 = productRepository.findById(product1.getId()).orElseThrow();

            assertThat(updated1.getQuantity()).isEqualTo(originQuantity - orderQuantity);
        }

        @ParameterizedTest
        @CsvSource({
                "10, 11",
                "10, 12"
        })
        @DisplayName("재고 차감 실패 - 수량 부족")
        void 실패_수량_부족(int originQuantity, int orderQuantity) {
            //given
            Product product1 = productRepository.save(ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품1", 1000, originQuantity));

            List<OrderProductCommand> commands = List.of(
                    new OrderProductCommand(product1.getId(), orderQuantity)
            );

            // when & then
            assertThatThrownBy(() -> productService.decreaseQuantity(commands))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INSUFFICIENT_QUANTITY.getMessage());

            Product updated1 = productRepository.findById(product1.getId()).orElseThrow();
            assertThat(updated1.getQuantity()).isEqualTo(originQuantity);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 상품")
        void 재고_차감_실패_존재하지_않는_상품() {
            //given
            List<OrderProductCommand> commands = List.of(
                    new OrderProductCommand(999L, 10)
            );

            // when & then
            assertThatThrownBy(() -> productService.decreaseQuantity(commands))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패 - 실패 된 경우 모두 롤백된다.")
        void 재고_차감_실패() {
            //given
            Product product1 = productRepository.save(ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품1", 1000, 10));
            Product product2 = productRepository.save(ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품2", 1000, 10));

            List<OrderProductCommand> commands = List.of(
                    new OrderProductCommand(product1.getId(), 10),
                    new OrderProductCommand(product2.getId(), 11)
            );

            // when & then
            assertThatThrownBy(() -> productService.decreaseQuantity(commands))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INSUFFICIENT_QUANTITY.getMessage());

            Product updated1 = productRepository.findById(product1.getId()).orElseThrow();
            Product updated2 = productRepository.findById(product2.getId()).orElseThrow();

            assertThat(updated1.getQuantity()).isEqualTo(product1.getQuantity());
            assertThat(updated2.getQuantity()).isEqualTo(product2.getQuantity());
        }
    }


    @Nested
    @DisplayName("3일간 상위 상품 5개 조회")
    class GetBestProductList {

        @Test
        @DisplayName("완료")
        void 상위_상품_5개_조회() {
            for (int i = 1; i <= 6; i++) {
                Product product = productRepository.save(
                        ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품" + i, 1000 * i, 100)
                );

                for (int d = 0; d < 3; d++) {
                    LocalDate date = LocalDate.now().minusDays(d);
                    int salesQuantity = 10 * i;

                    productDailySalesRepository.save(
                            ProductDailySales.builder()
                                    .productId(product.getId())
                                    .salesDate(date)
                                    .quantity(salesQuantity)
                                    .build()
                    );
                }
            }

            // when
            List<BestProductResult> bestProducts = productService.getBest();

            // then
            assertThat(bestProducts).hasSize(5);
            assertThat(bestProducts.get(0).productName()).isEqualTo("상품6");
            assertThat(bestProducts.get(4).productName()).isEqualTo("상품2");
        }
    }
}
