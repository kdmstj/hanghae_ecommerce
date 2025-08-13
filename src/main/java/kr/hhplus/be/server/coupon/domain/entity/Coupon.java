package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.domain.DiscountType;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Enumerated(value = EnumType.STRING)
    private DiscountType discountType;

    private int discountValue;

    private LocalDateTime issuedStartedAt;

    private LocalDateTime issuedEndedAt;

    private LocalDateTime createdAt;

    public void validateIssuePeriod() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(issuedStartedAt)) {
            throw new BusinessException(ErrorCode.ISSUE_PERIOD_NOT_STARTED);
        }
        if (now.isAfter(issuedEndedAt)) {
            throw new BusinessException(ErrorCode.ISSUE_PERIOD_ENDED);
        }
    }
}
