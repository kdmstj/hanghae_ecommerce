package kr.hhplus.be.server.point.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.point.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PointHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long userPointId;

    long orderId;

    int amount;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    LocalDateTime createdAt;

    public static PointHistory createChargeHistory(long userPointId, int amount){
        return PointHistory.builder()
                .userPointId(userPointId)
                .amount(amount)
                .transactionType(TransactionType.CHARGE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PointHistory createUseHistory(long orderId, long userPointId, int amount){
        return PointHistory.builder()
                .userPointId(userPointId)
                .orderId(orderId)
                .amount(-amount)
                .transactionType(TransactionType.USE)
                .createdAt(LocalDateTime.now())
                .build();
    }

}


