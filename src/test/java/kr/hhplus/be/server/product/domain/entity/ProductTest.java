package kr.hhplus.be.server.product.domain.entity;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ProductTest {

    @Nested
    @DisplayName("재고 감소")
    class DecreaseQuantity {
        @ParameterizedTest
        @CsvSource({
                "10, 10",
                "10, 9"
        })
        @DisplayName("완료")
        void 재고_감소_완료(int originalQuantity, int decreaseQuantity) {
            //given
            Product product = ProductFixture.withQuantity(originalQuantity);

            //when
            product.decreaseQuantity(decreaseQuantity);

            //then
            assertThat(product.getQuantity()).isEqualTo(originalQuantity - decreaseQuantity);
        }

        @DisplayName("실패 - 재고 부족")
        @ParameterizedTest
        @CsvSource({
                "10, 11",
                "10, 12"
        })
        void 재고_감소_실패_재고_부족(int originalQuantity, int decreaseQuantity) {
            //given
            Product product = ProductFixture.withQuantity(originalQuantity);

            //when & then
            assertThatThrownBy(() -> product.decreaseQuantity(decreaseQuantity))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INSUFFICIENT_QUANTITY.getMessage());
        }
    }
}
