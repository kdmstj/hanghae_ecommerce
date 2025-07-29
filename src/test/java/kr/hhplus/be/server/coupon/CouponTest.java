package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CouponTest {

    @Nested
    @DisplayName("쿠폰 발급")
    class IssueCoupon {

        @ParameterizedTest(name = "성공 - total: {0}, issued: {1}")
        @CsvSource({
                "100, 99",
                "100, 98"
        })
        @DisplayName("성공 - 수량 조건 만족 시 발급 성공")
        void 쿠폰_발급_성공(int totalQuantity, int issuedQuantity) {
            Coupon coupon = CouponFixture.withTotalQuantityAndIssuedQuantity(totalQuantity, issuedQuantity);
            coupon.increaseIssuedQuantity();
            assertThat(coupon.getIssuedQuantity()).isEqualTo(issuedQuantity + 1);
        }

        @ParameterizedTest(name = "실패 - 수량 초과 total: {0}, issued: {1}")
        @CsvSource({
                "100, 100",
                "10, 10"
        })
        @DisplayName("실패 - 수량 초과 시 예외 발생")
        void 쿠폰_발급_실패_수량_초과(int totalQuantity, int issuedQuantity) {
            Coupon coupon = CouponFixture.withTotalQuantityAndIssuedQuantity(totalQuantity, issuedQuantity);
            assertThatThrownBy(coupon::increaseIssuedQuantity)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.EXCEED_QUANTITY.getMessage());
        }

        static Stream<Arguments> timeBoundarySuccessCases() {
            LocalDateTime now = LocalDateTime.now();
            return Stream.of(
                    Arguments.of("성공 - 시작 시간과 동일", now, now.plusMinutes(5)),
                    Arguments.of("성공 - 종료 1초 후", now.minusMinutes(1), now.plusSeconds(1))
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("timeBoundarySuccessCases")
        @DisplayName("성공 - 경계 시간 포함 테스트")
        void 쿠폰_발급_성공_시간_경계(String title, LocalDateTime start, LocalDateTime end) {
            Coupon coupon = CouponFixture.withIssuedQuantityAndIssuedStartedAtAndIssuedEndedAt(0, start, end);
            coupon.increaseIssuedQuantity();
            assertThat(coupon.getIssuedQuantity()).isEqualTo(1);
        }

        static Stream<Arguments> timeBoundaryFailCases() {
            LocalDateTime now = LocalDateTime.now();
            return Stream.of(
                    Arguments.of("실패 - 시작 1초 전", now.plusSeconds(1), now.plusMinutes(1), ErrorCode.ISSUE_PERIOD_NOT_STARTED),
                    Arguments.of("실패 - 종료 1초 전", now.minusMinutes(5), now.minusSeconds(1), ErrorCode.ISSUE_PERIOD_ENDED)
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("timeBoundaryFailCases")
        @DisplayName("실패 - 시간 조건 위반 테스트")
        void 쿠폰_발급_실패_시간_조건(String title, LocalDateTime start, LocalDateTime end, ErrorCode errorCode) {
            Coupon coupon = CouponFixture.withIssuedQuantityAndIssuedStartedAtAndIssuedEndedAt(0, start, end);
            assertThatThrownBy(coupon::increaseIssuedQuantity)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(errorCode.getMessage());
        }
    }
}
