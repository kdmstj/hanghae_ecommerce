package kr.hhplus.be.server.point.application.result;

import kr.hhplus.be.server.point.domain.entity.UserPoint;

public record UserPointResult(
        long id,
        long userId,
        int balance
) {
    public static UserPointResult from(UserPoint userPoint){
        return new UserPointResult(
                userPoint.getId(),
                userPoint.getUserId(),
                userPoint.getBalance()
        );
    }
}
