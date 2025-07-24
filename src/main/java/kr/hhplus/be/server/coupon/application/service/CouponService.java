package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;

    public List<UserCoupon> getValidCoupons(long userId) {

        return userCouponRepository.findAllByUserIdAndUsedAtIsNullAndExpiredAtAfter(userId, LocalDateTime.now());
    }

    @Transactional
    public UserCoupon issue(long userId, long couponId) {

        Coupon coupon = get(couponId);

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        coupon.issue();
        UserCoupon issuedCoupon = UserCoupon.create(userId, couponId, coupon.getIssuedEndedAt());

        return userCouponRepository.save(issuedCoupon);
    }

    private Coupon get(long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() ->
                new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

}
