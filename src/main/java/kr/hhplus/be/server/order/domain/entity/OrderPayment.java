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
@Builder
@Getter
public class OrderPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long orderId;

    int orderAmount;

    int discountAmount;

    int paymentAmount;

    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime updatedAt = LocalDateTime.now();

    public static OrderPayment create(long orderId, int orderAmount, int discountAmount, int paymentAmount){
        return OrderPayment.builder()
                .orderId(orderId)
                .orderAmount(orderAmount)
                .discountAmount(discountAmount)
                .paymentAmount(paymentAmount)
                .build();
    }
}
