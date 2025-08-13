package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.fixture.CouponQuantityFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class CouponQuantityTest {

    @Nested
    @DisplayName("쿠폰 발급 수량 증가")
    class IncreaseIssuedQuantity {
        @ParameterizedTest(name = "성공 - total: {0}, issued: {1}")
        @CsvSource({
                "100, 99",
                "100, 98"
        })
        @DisplayName("성공 - 수량 조건 만족 시 발급 성공")
        void 쿠폰_발급_성공(int totalQuantity, int issuedQuantity) {
            CouponQuantity couponQuantity = CouponQuantityFixture.withTotalQuantityAndIssuedQuantity(totalQuantity, issuedQuantity);
            couponQuantity.increaseIssuedQuantity();
            assertThat(couponQuantity.getIssuedQuantity()).isEqualTo(issuedQuantity + 1);
        }

        @ParameterizedTest(name = "실패 - 수량 초과 total: {0}, issued: {1}")
        @CsvSource({
                "100, 100",
                "10, 10"
        })
        @DisplayName("실패 - 수량 초과 시 예외 발생")
        void 쿠폰_발급_실패_수량_초과(int totalQuantity, int issuedQuantity) {
            CouponQuantity couponQuantity = CouponQuantityFixture.withTotalQuantityAndIssuedQuantity(totalQuantity, issuedQuantity);
            assertThatThrownBy(couponQuantity::increaseIssuedQuantity)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.EXCEED_QUANTITY.getMessage());
        }
    }
}
