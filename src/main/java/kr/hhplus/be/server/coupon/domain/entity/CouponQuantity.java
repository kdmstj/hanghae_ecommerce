package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class CouponQuantity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long couponId;

    private int totalQuantity;

    private int issuedQuantity;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    public void increaseIssuedQuantity() {
        if (issuedQuantity >= totalQuantity) {
            throw new BusinessException(ErrorCode.EXCEED_QUANTITY);
        }

        this.issuedQuantity += 1;
        this.updatedAt = LocalDateTime.now();
    }
}
