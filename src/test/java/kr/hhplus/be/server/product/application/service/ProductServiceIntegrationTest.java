package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

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
    private DataBaseCleanUp dataBaseCleanUp;

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
}
