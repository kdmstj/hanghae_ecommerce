package kr.hhplus.be.server.point.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.point.application.command.PointChargeCommand;
import kr.hhplus.be.server.point.application.command.PointUseCommand;
import kr.hhplus.be.server.point.domain.TransactionType;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoint {

        @Test
        @DisplayName("포인트 충전 시 유저의 포인트가 증가하고 이력이 기록된다")
        void 포인트_충전_검증() {
            // given
            long userId = 1L;
            int originAmount = 5_000;
            int chargeAmount = 1_000;
            UserPoint userPoint = userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));


            // when
            PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);
            pointService.charge(command);

            // then
            assertThat(userPointRepository.findOneByUserId(userId).get().getBalance())
                    .isEqualTo(originAmount + chargeAmount);

            assertThat(pointHistoryRepository.findAll())
                    .hasSize(1)
                    .allSatisfy(history -> {
                        assertThat(history.getUserPointId()).isEqualTo(userPoint.getUserId());
                        assertThat(history.getAmount()).isEqualTo(chargeAmount);
                        assertThat(history.getTransactionType()).isEqualTo(TransactionType.CHARGE);
                    });
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 포인트 정보")
        void 포인트_충전_실패_존재하지_않는_포인트_정보() {
            // given
            int chargeAmount = 1000;
            PointChargeCommand command = new PointChargeCommand(999L, chargeAmount);

            // when & then
            assertThatThrownBy(() -> pointService.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.USER_POINT_NOT_FOUND.getMessage());
        }


        @Test
        @DisplayName("실패 - 최대 보유 잔고 금액 초과")
        void 포인트_충전_실패_최대_보유_잔고_금액_초과() {
            // given
            long userId = 1L;
            int originAmount = UserPoint.MAX_BALANCE;
            int chargeAmount = 1_000;

            userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));
            PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);

            // when & then
            assertThatThrownBy(() -> pointService.charge(command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.EXCEED_MAX_BALANCE.getMessage());
        }
    }


    @Nested
    @DisplayName("포인트 차감")
    class UsePoint {

        @Test
        @DisplayName("포인트 차감 시 유저의 포인트가 차감되고 이력이 기록된다")
        void 포인트_차감_성공() {
            //given
            long userId = 1L;
            int originAmount = 20_000;
            int useAmount = 10_000;

            UserPoint userPoint = userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));
            PointUseCommand command = new PointUseCommand(userId, useAmount);

            //when
            long orderId = 1L;
            pointService.use(orderId, command);

            //then
            assertThat(userPointRepository.findOneByUserId(userId).get().getBalance())
                    .isEqualTo(originAmount - useAmount);

            assertThat(pointHistoryRepository.findAll())
                    .hasSize(1)
                    .allSatisfy(history -> {
                        assertThat(history.getUserPointId()).isEqualTo(userPoint.getUserId());
                        assertThat(history.getAmount()).isEqualTo(-useAmount);
                        assertThat(history.getTransactionType()).isEqualTo(TransactionType.USE);
                    });
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 포인트 정보")
        void 포인트_충전_실패_존재하지_않는_포인트_정보() {
            // given
            PointUseCommand command = new PointUseCommand(999L, 10_000);

            // when & then
            assertThatThrownBy(() -> pointService.use(1L, command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.USER_POINT_NOT_FOUND.getMessage());
        }


        @Test
        @DisplayName("실패 - 보유 잔액 부족")
        void 포인트_충전_실패_보유_잔액_부족() {
            // given
            long userId = 1L;
            int originAmount = 0;
            int useAmount = 10_000;

            userPointRepository.save(UserPointFixture.withUserIdAndBalance(userId, originAmount));
            PointUseCommand command = new PointUseCommand(userId, useAmount);

            // when & then
            long orderId = 1L;
            assertThatThrownBy(() -> pointService.use(orderId, command))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.INSUFFICIENT_BALANCE.getMessage());

            assertThat(pointHistoryRepository.findAll()).hasSize(0);

        }
    }
}
