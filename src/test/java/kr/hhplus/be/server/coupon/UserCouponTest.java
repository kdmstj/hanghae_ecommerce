package kr.hhplus.be.server.coupon;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.fixture.UserCouponFixture;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserCouponTest {

    @Nested
    @DisplayName("쿠폰 사용")
    class UseCoupon {

        @Test
        @DisplayName("성공")
        void 쿠폰_사용_성공(){
            //given
            UserCoupon userCoupon = UserCouponFixture.withExpiredAt(LocalDateTime.now().plusWeeks(1));

            //when
            long orderId = 1L;
            int discountAmount = 1000;
            userCoupon.use(orderId, discountAmount);

            //then
            assertThat(userCoupon.getOrderId()).isEqualTo(orderId);
            assertThat(userCoupon.getDiscountAmount()).isEqualTo(discountAmount);
            assertThat(userCoupon.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - 만료 기간이 지났을 경우")
        void 쿠폰_사용_실패_만료_기간이_지났을_경우(){
            //given
            UserCoupon userCoupon = UserCouponFixture.withExpiredAt(LocalDateTime.now().minusSeconds(1));

            //when & then
            long orderId = 1L;
            int discountAmount = 1000;
            Assertions.assertThatThrownBy(() -> userCoupon.use(orderId, discountAmount))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_EXPIRED.getMessage());
        }

        @Test
        @DisplayName("실패 - 이미 사용한 경우")
        void 쿠폰_사용_실패_이미_사용한_경우(){
            //given
            UserCoupon userCoupon = UserCouponFixture.withUsedAt(LocalDateTime.now().minusMinutes(1));

            //when & then
            long orderId = 1L;
            int discountValue = 1000;
            Assertions.assertThatThrownBy(() -> userCoupon.use(orderId, discountValue))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());
        }
    }
}
