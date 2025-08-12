package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import kr.hhplus.be.server.coupon.fixture.CouponQuantityFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponQuantityRepository couponQuantityRepository;

    @DisplayName("동시에 쿠폰 발급을 받는 경우 정상적으로 적용된다.")
    @Test
    void 동시에_쿠폰_발급을_받는_경우_정상적으로_적용된다() throws Exception {
        //given
        Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
        int issuedQuantity = 0;
        int totalQuantity = 100;
        couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), totalQuantity, issuedQuantity));

        //when
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executorService.submit(() -> {
                try {
                    couponService.issue((long) userId, coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        CouponQuantity couponQuantity = couponQuantityRepository.findOneByCouponId(coupon.getId());
        assertThat(couponQuantity.getIssuedQuantity()).isEqualTo(threadCount);
    }
}
