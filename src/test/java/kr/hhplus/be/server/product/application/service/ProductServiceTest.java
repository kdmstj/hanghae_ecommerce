package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProductList {
        @Test
        @DisplayName("상품이 전체 조회된다")
        void 상품_목록_조회() {
            // given
            List<Product> productList = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                productList.add(ProductFixture.withProductNameAndPricePerUnitAndQuantity(
                        "productName" + i, 100 * (i + 1), 10 * (i + 1)));
            }
            productRepository.saveAll(productList);

            // when
            List<ProductResult> result = productService.get();

            // then
            assertThat(result).hasSize(5);
            for (int i = 0; i < 5; i++) {
                ProductResult productResult = result.get(i);
                assertThat(productResult.productName()).isEqualTo("productName" + i);
                assertThat(productResult.pricePerUnit()).isEqualTo(100 * (i + 1));
                assertThat(productResult.quantity()).isEqualTo(10 * (i + 1));
            }
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class GetProduct {
        @Test
        @DisplayName("상품 ID로 조회 성공")
        void 상품_상세_조회_완료() {
            // given
            String productName = "productName";
            int pricePerUnit = 1000;
            int quantity = 100;
            Product product = productRepository.save(
                    ProductFixture.withProductNameAndPricePerUnitAndQuantity(productName, pricePerUnit, quantity)
            );

            // when
            ProductResult result = productService.get(product.getId());

            // then
            assertThat(result.productName()).isEqualTo(productName);
            assertThat(result.pricePerUnit()).isEqualTo(pricePerUnit);
            assertThat(result.quantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID는 예외 발생")
        void 상품_상세_조회_실패() {
            // when & then
            assertThatThrownBy(() -> productService.get(999L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }
}
