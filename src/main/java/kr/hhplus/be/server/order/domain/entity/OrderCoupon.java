package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class OrderCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long orderId;

    long userCouponId;

    int discountAmount;

    LocalDateTime createdAt;

    public static OrderCoupon create(long orderId, long userCouponId, int discountAmount){
        return OrderCoupon.builder()
                .orderId(orderId)
                .userCouponId(userCouponId)
                .discountAmount(discountAmount)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
