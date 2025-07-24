package kr.hhplus.be.server.point.presentation.dto;

import jakarta.validation.constraints.Min;
import kr.hhplus.be.server.point.application.command.PointChargeCommand;

public record UserPointRequest(
        @Min(value = 10_000)
        int amount
) {
    public PointChargeCommand toCommand(Long userId){
        return new PointChargeCommand(userId, amount);
    }
}
