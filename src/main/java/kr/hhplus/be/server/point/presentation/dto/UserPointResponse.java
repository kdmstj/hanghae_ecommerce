package kr.hhplus.be.server.point.presentation.dto;

import kr.hhplus.be.server.point.application.result.UserPointResult;
import lombok.Builder;

@Builder
public record UserPointResponse(
        long id,
        long userId,
        int balance
) {
    public static UserPointResponse from(UserPointResult result) {
        return new UserPointResponse(
                result.id(),
                result.userId(),
                result.balance()
        );
    }
}
