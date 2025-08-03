package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class CouponTest {

    @Nested
    @DisplayName("쿠폰 발급 유효성 체크")
    class IssueCoupon {

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
            // given
            Coupon coupon = CouponFixture.withIssuedStartedAtAndIssuedEndedAt(start, end);

            // when & then
            assertThatCode(coupon::validateIssuePeriod)
                    .doesNotThrowAnyException();
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
            // given
            Coupon coupon = CouponFixture.withIssuedStartedAtAndIssuedEndedAt(start, end);

            // when & then
            assertThatThrownBy(coupon::validateIssuePeriod)
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(errorCode.getMessage());
        }
    }
}
