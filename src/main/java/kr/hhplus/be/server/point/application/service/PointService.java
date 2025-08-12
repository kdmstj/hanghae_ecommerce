package kr.hhplus.be.server.point.application.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.point.application.command.PointChargeCommand;
import kr.hhplus.be.server.point.application.command.PointUseCommand;
import kr.hhplus.be.server.point.application.result.UserPointResult;
import kr.hhplus.be.server.point.domain.entity.PointHistory;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public UserPointResult get(long userId) {

        return UserPointResult.from(find(userId));
    }

    @Transactional
    public UserPointResult charge(PointChargeCommand command) {
        try {
            UserPoint userPoint = findWithOptimisticLock(command.userId());

            userPoint.increaseBalance(command.amount());
            pointHistoryRepository.save(PointHistory.createChargeHistory(userPoint.getId(), command.amount()));

            UserPoint result = userPointRepository.saveAndFlush(userPoint);
            return UserPointResult.from(result);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BusinessException(ErrorCode.CONFLICT_CHARGE);
        }
    }

    @Transactional
    public UserPointResult use(Long orderId, PointUseCommand command){
        UserPoint userPoint = findWithPessimisticLock(command.userId());

        userPoint.decreaseBalance(command.amount());
        pointHistoryRepository.save(PointHistory.createUseHistory(orderId, userPoint.getId(), command.amount()));

        return UserPointResult.from(userPoint);
    }

    private UserPoint find(long userId){
        return userPointRepository.findOneByUserId(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }

    private UserPoint findWithOptimisticLock(long userId){
        return userPointRepository.findWithOptimisticLock(userId).orElseThrow(() ->
            new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }

    private UserPoint findWithPessimisticLock(long userId){
        return userPointRepository.findWithPessimisticLock(userId).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));
    }
}
