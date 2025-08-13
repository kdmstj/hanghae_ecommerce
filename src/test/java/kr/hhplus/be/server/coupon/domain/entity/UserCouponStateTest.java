package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.fixture.UserCouponStateFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserCouponStateTest {

    @Nested
    @DisplayName("쿠폰 사용 상태 변경")
    class UpdateStatusToUse{
        @Test
        @DisplayName("성공")
        void 쿠폰_사용_상태_변경_성공(){
            //given
            UserCouponState userCouponState = UserCouponStateFixture.withUserCouponStatus(UserCouponStatus.ISSUED);

            //when
            userCouponState.update(UserCouponStatus.USED);

            //then
            assertThat(userCouponState.getUserCouponStatus()).isEqualTo(UserCouponStatus.USED);
        }

        @Test
        @DisplayName("실패 - 만료 기간이 지났을 경우")
        void 쿠폰_사용_상태_변경_실패_만료되었을_경우(){
            //given
            UserCouponState userCouponState = UserCouponStateFixture.withUserCouponStatus(UserCouponStatus.EXPIRED);

            //when & then
            Assertions.assertThatThrownBy(() -> userCouponState.update(UserCouponStatus.USED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_EXPIRED.getMessage());
        }

        @Test
        @DisplayName("실패 - 이미 상태가 사용인 경우")
        void 쿠폰_사용_상태_변경_실패_이미_사용되었을_경우(){
            //given
            UserCouponState userCouponState = UserCouponStateFixture.withUserCouponStatus(UserCouponStatus.USED);

            //when & then
            Assertions.assertThatThrownBy(() -> userCouponState.update(UserCouponStatus.USED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());
        }
    }
}
