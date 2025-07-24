package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long userId;

    long couponId;

    long orderId;

    int discountAmount;

    LocalDateTime issuedAt;

    LocalDateTime expiredAt;

    LocalDateTime usedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    public static UserCoupon create(long userId, long couponId, LocalDateTime expiredAt){
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void use(long orderId, int discountAmount){
        if(usedAt != null){
            throw new BusinessException(ErrorCode.ALREADY_USED);
        }

        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new BusinessException(ErrorCode.ALREADY_EXPIRED);
        }

        this.orderId = orderId;
        this.discountAmount = discountAmount;
        this.usedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
