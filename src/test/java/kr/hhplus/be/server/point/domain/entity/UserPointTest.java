package kr.hhplus.be.server.point.domain.entity;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserPointTest {

    @Nested
    @DisplayName("포인트 충전")
    class Charge {

        @ParameterizedTest
        @CsvSource({
                "1_999_999, 1",
                "1_900_000, 100_000",
                "0, 2_000_000"
        })
        @DisplayName("성공")
        void 포인트_충전_성공(int originAmount, int chargeAmount) {
            // given
            UserPoint userPoint = UserPointFixture.withBalance(originAmount);

            // when
            userPoint.increaseBalance(chargeAmount);

            // then
            assertThat(userPoint.getBalance()).isEqualTo(originAmount + chargeAmount);
        }

        @ParameterizedTest
        @CsvSource({
                "2_000_000, 1",
                "1_999_999, 2",
                "1_500_000, 600_000"
        })
        @DisplayName("실패 - 최대 잔고를 초과하면 실패한다")
        void 포인트_충전_예외(int originAmount, int chargeAmount) {
            // given
            UserPoint userPoint = UserPointFixture.withBalance(originAmount);

            // when & then
            assertThatThrownBy(() -> userPoint.increaseBalance(chargeAmount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.EXCEED_MAX_BALANCE.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 사용")
    class Use {

        @ParameterizedTest
        @CsvSource({
                "10000, 10000",
                "10000, 9999"
        })
        @DisplayName("성공")
        void 포인트_사용_성공(int originAmount, int useAmount) {
            //given
            UserPoint userPoint = UserPointFixture.withBalance(originAmount);

            //when
            userPoint.decreaseBalance(useAmount);

            //then
            assertThat(userPoint.getBalance()).isEqualTo(originAmount - useAmount);
        }

        @ParameterizedTest
        @CsvSource({
                "10000, 10001",
                "10000, 10002"
        })
        @DisplayName("실패 - 잔액이 부족하면 실패한다.")
        void 포인트_사용_예외(int originAmount, int useAmount) {
            //given
            UserPoint userPoint = UserPointFixture.withBalance(originAmount);

            //when & then
            assertThatThrownBy(() -> userPoint.decreaseBalance(useAmount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
        }
    }
}
