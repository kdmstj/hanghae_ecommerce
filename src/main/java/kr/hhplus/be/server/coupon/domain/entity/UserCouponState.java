package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class UserCouponState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long userCouponId;

    @Enumerated(value = EnumType.STRING)
    UserCouponStatus userCouponStatus;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    public void update(UserCouponStatus userCouponStatus){
        if(this.userCouponStatus == UserCouponStatus.USED){
            throw new BusinessException(ErrorCode.ALREADY_USED);
        }

        if(this.userCouponStatus == UserCouponStatus.EXPIRED){
            throw new BusinessException(ErrorCode.ALREADY_EXPIRED);
        }

        this.userCouponStatus = userCouponStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
