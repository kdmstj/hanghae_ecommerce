package kr.hhplus.be.server.point;

import kr.hhplus.be.server.point.application.command.PointChargeCommand;
import kr.hhplus.be.server.point.application.service.PointService;
import kr.hhplus.be.server.point.domain.entity.PointHistory;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    PointService pointService;

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoint {

        @Test
        @DisplayName("포인트 충전 시 유저의 포인트가 증가하고 이력이 기록된다")
        void 포인트_충전_검증() {
            // given
            long userId = 1L;
            int chargeAmount = 1_000;
            UserPoint current = UserPointFixture.withUserIdAndBalance(userId, 5_000);

            given(userPointRepository.findOneByUserId(userId))
                    .willReturn(Optional.of(current));

            PointChargeCommand command = new PointChargeCommand(userId, chargeAmount);

            // when
            pointService.charge(command);

            // then
            verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));
        }
    }
}
