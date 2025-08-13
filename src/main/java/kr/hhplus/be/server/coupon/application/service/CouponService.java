package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponStateRepository;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final UserCouponRepository userCouponRepository;
    private final UserCouponStateRepository userCouponStateRepository;
    private final CouponRepository couponRepository;
    private final CouponQuantityRepository couponQuantityRepository;

    public List<UserCouponResult> getValidCoupons(long userId) {

        return userCouponRepository.findAllValidByUserId(userId)
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
        CouponQuantity couponQuantity = couponQuantityRepository.findWithPessimisticLock(couponId);

        coupon.validateIssuePeriod();
        couponQuantity.increaseIssuedQuantity();

        UserCoupon issuedCoupon = UserCoupon.create(userId, couponId, coupon.getIssuedEndedAt());

        return UserCouponResult.from(userCouponRepository.save(issuedCoupon));
    }

    @Transactional
    public void use(List<CouponUseCommand> commands) throws ObjectOptimisticLockingFailureException {
        for (CouponUseCommand command : commands) {
            UserCouponState userCouponState = userCouponStateRepository.findOneByUserCouponId(command.userCouponId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_COUPON_NOT_FOUND));

            userCouponState.update(UserCouponStatus.USED);
            userCouponStateRepository.saveAndFlush(userCouponState);
        }
    }

    private Coupon getCoupon(long id) {
        return couponRepository.findById(id).orElseThrow(() ->
                new BusinessException(ErrorCode.COUPON_NOT_FOUND));
    }

}
