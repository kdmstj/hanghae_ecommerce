package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.fixture.UserCouponFixture;
import kr.hhplus.be.server.coupon.fixture.UserCouponStateFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class UserCouponRepositoryTest {

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserCouponStateRepository userCouponStateRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("findAllValidByUserId")
    class FindAllValidByUserIdTest {

        @Test
        @DisplayName("userId와 상태가 ISSUED인 쿠폰만 조회된다")
        void 성공() {
            // given
            long userId = 1L;

            for (int i = 1; i <= 3; i++) {
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, i));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));
            }

            UserCoupon expiredCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 4));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(expiredCoupon.getId(), UserCouponStatus.EXPIRED));

            UserCoupon usedCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 5));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(usedCoupon.getId(), UserCouponStatus.USED));

            // when
            List<UserCoupon> result = userCouponRepository.findAllValidByUserId(userId);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(c -> c.getUserId() == userId);
        }

        @Test
        @DisplayName("해당 userId에 대해 ISSUED 쿠폰이 하나도 없으면 빈 리스트를 반환한다")
        void 실패_ISSUED없음() {
            // given
            long userId = 2L;

            for (int i = 1; i <= 3; i++) {
                UserCoupon coupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, i));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(coupon.getId(), UserCouponStatus.USED));
            }

            // when
            List<UserCoupon> result = userCouponRepository.findAllValidByUserId(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndCouponId")
    class ExistsByUserIdAndCouponIdTest {

        @Test
        @DisplayName("userId와 couponId로 존재 여부를 확인할 수 있다")
        void 성공() {
            // given
            long userId = 1L;
            long couponId = 100L;

            userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, couponId));

            // when & then
            assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, couponId)).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 couponId 혹은 userId 조합은 false를 반환한다")
        void 실패_존재하지않음() {
            // given
            long userId = 1L;
            long couponId = 100L;

            userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, couponId));

            // when & then
            assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, 999L)).isFalse(); // 잘못된 couponId
            assertThat(userCouponRepository.existsByUserIdAndCouponId(2L, couponId)).isFalse(); // 잘못된 userId
            assertThat(userCouponRepository.existsByUserIdAndCouponId(2L, 999L)).isFalse();     // 둘 다 틀림
        }
    }
}
