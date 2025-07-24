package kr.hhplus.be.server.point.domain.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class UserPoint {

    public static final int MAX_BALANCE = 2_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    private int balance;

    private LocalDateTime updatedAt;

    public void charge(int chargeAmount) {
        if (balance + chargeAmount > MAX_BALANCE) {
            throw new BusinessException(ErrorCode.EXCEED_MAX_BALANCE);
        }
        this.balance += chargeAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public void use(int useAmount) {
        if (balance - useAmount < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance -= useAmount;
        this.updatedAt = LocalDateTime.now();
    }
}
