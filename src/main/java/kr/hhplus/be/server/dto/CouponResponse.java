package kr.hhplus.be.server.dto;

import java.time.LocalDateTime;

public record CouponResponse(
        long id,
        String name,
        String discountType,
        int discountValue,
        int issuedQuantity,
        LocalDateTime issuedStartedAt,
        LocalDateTime issuedEndedAt
) {
}
