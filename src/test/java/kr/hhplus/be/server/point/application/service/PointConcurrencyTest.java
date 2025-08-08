package kr.hhplus.be.server.point.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.point.application.command.PointChargeCommand;
import kr.hhplus.be.server.point.application.command.PointUseCommand;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class PointConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    void setUp() {
        dataBaseCleanUp.execute();
    }


    @DisplayName("동시에 포인트 충전시 정상적으로 충전된다.")
    @Test
    void 동시에_포인트_충전시_정상적으로_충전된다() throws Exception {
        //given
        long userId = 1L;
        int originAmount = 0;
        userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));

        //when
        int threadCount = 10;
        int chargeAmount = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger failCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {

            executorService.submit(() -> {
                try {
                    pointService.charge(new PointChargeCommand(userId, chargeAmount));
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                } finally
                {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        UserPoint result = userPointRepository.findOneByUserId(userId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(originAmount + chargeAmount * (threadCount - failCount.get()));
    }

    @DisplayName("동시에 포인트 차감 시 정상적으로 차감된다.")
    @Test
    void 동시에_포인트_차감시_정상적으로_차감된다() throws Exception {
        //given
        long userId = 1L;
        int originAmount = 100000;
        userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));

        //when
        int threadCount = 10;
        int useAmount = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {

            executorService.submit(() -> {
                try {
                    pointService.use(1L, new PointUseCommand(userId, useAmount));
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        //then
        UserPoint result = userPointRepository.findOneByUserId(userId).orElseThrow();
        assertThat(result.getBalance()).isEqualTo(originAmount - useAmount * threadCount);
    }
}
