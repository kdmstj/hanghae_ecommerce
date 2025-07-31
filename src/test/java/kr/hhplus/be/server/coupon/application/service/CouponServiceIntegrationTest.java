package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponStateRepository;
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

@SpringBootTest
@Testcontainers
public class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserCouponStateRepository userCouponStateRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponQuantityRepository couponQuantityRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("유효한 보유 쿠폰 목록 조회")
    class GetValidCoupons {


        @Test
        @DisplayName("완료")
        void 유효한_보유_쿠폰_목록_조회() {
            //given
            long userId = 1L;
            for (int i = 1; i <= 10; i++) {
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, i));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));
            }

            UserCoupon usedUserCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 11));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(usedUserCoupon.getId(), UserCouponStatus.USED));

            UserCoupon expiredUserCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 12));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(expiredUserCoupon.getId(), UserCouponStatus.EXPIRED));


            //when
            List<UserCouponResult> result = couponService.getValidCoupons(userId);

            //then
            assertThat(result).hasSize(10);
            assertThat(result)
                    .extracting(UserCouponResult::couponId)
                    .doesNotContain(11L, 12L);
        }
    }
}
