package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.domain.repository.CouponIssueCacheRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import kr.hhplus.be.server.coupon.fixture.CouponQuantityFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private CouponIssueCacheRepository couponIssueCacheRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @Autowired
    private RedisTemplate redisTemplate;

    @BeforeEach()
    void setUp(){
        dataBaseCleanUp.execute();
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushAll();
    }

    @Nested
    @DisplayName("동시 쿠폰 발급 요청")
    class RequestIssueTest {

        @Test
        @DisplayName("동시에 쿠폰 발급 요청 시 Redis 큐에 정상적으로 쌓인다")
        void 동시에_쿠폰_발급_요청() throws Exception {
            // given
            Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
            couponQuantityRepository.save(
                    CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, 0)
            );

            int threadCount = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        couponService.requestIssue(userId, coupon.getId());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            long issuedCount = couponIssueCacheRepository.countIssuedUser(coupon.getId());
            assertThat(issuedCount).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("스케줄러 실행 후 발급 확정")
    class IssueConfirmTest {

        @Test
        @DisplayName("동시에 쿠폰 발급 요청 후 스케줄러 실행 시 DB에 발급 반영된다")
        void 동시에_쿠폰_발급_후_스케줄러_실행() throws Exception {
            // given
            Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
            couponQuantityRepository.save(
                    CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, 0)
            );

            int threadCount = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final long userId = i + 1L;
                executorService.submit(() -> {
                    try {
                        couponService.requestIssue(userId, coupon.getId());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executorService.shutdown();

            couponService.issuePendingCoupons(100);

            CouponQuantity cq = couponQuantityRepository.findOneByCouponId(coupon.getId());
            assertThat(cq.getIssuedQuantity()).isEqualTo(threadCount);
            assertThat(userCouponRepository.count()).isEqualTo(threadCount);
        }
    }
}
