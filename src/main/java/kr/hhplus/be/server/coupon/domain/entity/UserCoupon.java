package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    LocalDateTime issuedAt;

    LocalDateTime expiredAt;

    LocalDateTime createdAt;

    public static UserCoupon create(long userId, long couponId, LocalDateTime expiredAt){
        return UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
