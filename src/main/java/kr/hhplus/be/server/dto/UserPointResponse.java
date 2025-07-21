package kr.hhplus.be.server.dto;

import lombok.Builder;

@Builder
public record UserPointResponse(
        long userId,
        int balance
) {
}
