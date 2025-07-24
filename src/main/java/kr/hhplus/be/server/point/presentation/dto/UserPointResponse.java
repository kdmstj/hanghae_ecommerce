package kr.hhplus.be.server.point.presentation.dto;

import kr.hhplus.be.server.point.domain.entity.UserPoint;
import lombok.Builder;

@Builder
public record UserPointResponse(
        long id,
        long userId,
        int balance
) {
    public static UserPointResponse from(UserPoint userPoint) {
        return new UserPointResponse(userPoint.getId(), userPoint.getUserId(), userPoint.getBalance());
    }
}
