package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;

    public List<UserCouponResult> getValidCoupons(long userId) {

        return userCouponRepository.findAllByUserIdAndUsedAtIsNullAndExpiredAtAfter(userId, LocalDateTime.now())
                .stream()
                .map(UserCouponResult::from)
                .toList();
    }

    @Transactional
    public UserCouponResult issue(long userId, long couponId) {

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        Coupon coupon = getCoupon(couponId);
        CouponQuantity couponQuantity = couponQuantityRepository.findOneByCouponId(couponId);

        coupon.validateIssuePeriod();
        couponQuantity.increaseIssuedQuantity();

        UserCoupon issuedCoupon = UserCoupon.create(userId, couponId, coupon.getIssuedEndedAt());

        return UserCouponResult.from(userCouponRepository.save(issuedCoupon));
    }

    public List<UserCoupon> use(long orderId, List<CouponUseCommand> commands){
        List<UserCoupon> usedCoupons = new ArrayList<>();

        for(CouponUseCommand command : commands){
            UserCoupon userCoupon = getUserCoupon(command.userCouponId());

            userCoupon.use(orderId, command.discountAmount());

            usedCoupons.add(userCoupon);
        }

        return usedCoupons;
    }

    private UserCoupon getUserCoupon(long id){
        return userCouponRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));
    }

    private Coupon getCoupon(long id) {
        return couponRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

}
