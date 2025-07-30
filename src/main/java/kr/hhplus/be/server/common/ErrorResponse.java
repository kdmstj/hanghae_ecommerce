package kr.hhplus.be.server.common;

public record ErrorResponse(
        String code,
        String message
) {
}
