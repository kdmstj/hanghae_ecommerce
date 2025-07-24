package kr.hhplus.be.server.point.application.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.point.application.command.PointChargeCommand;
import kr.hhplus.be.server.point.application.command.PointUseCommand;
import kr.hhplus.be.server.point.domain.entity.PointHistory;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public UserPoint get(long userId) {
        return userPointRepository.findOneByUserId(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }

    @Transactional
    public UserPoint charge(PointChargeCommand command) {
        UserPoint userPoint = get(command.userId());

        userPoint.charge(command.amount());
        pointHistoryRepository.save(PointHistory.createChargeHistory(userPoint.getId(), command.amount()));

        return userPoint;
    }

    @Transactional
    public UserPoint use(Long orderId, PointUseCommand command){
        UserPoint userPoint = get(command.userId());

        userPoint.use(command.amount());
        pointHistoryRepository.save(PointHistory.createUseHistory(orderId, userPoint.getId(), command.amount()));

        return userPoint;
    }
}
