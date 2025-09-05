package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.application.event.CouponEventPublisher;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import kr.hhplus.be.server.coupon.domain.repository.*;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponEventPublisher couponEventPublisher;
    private final UserCouponRepository userCouponRepository;
    private final UserCouponStateRepository userCouponStateRepository;
    private final CouponRepository couponRepository;
    private final CouponQuantityRepository couponQuantityRepository;
    private final CouponIssueCacheRepository couponIssueCacheRepository;

    public void requestIssue(long userId, long couponId) {
        couponEventPublisher.publish(new CouponIssueEvent(userId, couponId));
    }

    public List<UserCouponResult> getValidCoupons(long userId) {

        return userCouponRepository.findAllValidByUserId(userId)
                .stream()
                .map(UserCouponResult::from)
                .toList();
    }

    @Transactional
    public void handleCouponIssue(long userId, long couponId) {
        Coupon coupon = getCoupon(couponId);
        coupon.validateIssuePeriod();

        if (couponIssueCacheRepository.existsIssuedUser(couponId, userId)) {
            throw new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON);
        }

        int limitQuantity = couponIssueCacheRepository.getCouponLimitQuantity(couponId);
        long issuedCount = couponIssueCacheRepository.countIssuedUser(couponId);
        if (limitQuantity <= issuedCount) {
            throw new BusinessException(ErrorCode.EXCEED_QUANTITY);
        }

        couponIssueCacheRepository.saveIssuedUser(couponId, userId);

        CouponQuantity couponQuantity = couponQuantityRepository.findWithPessimisticLock(couponId);
        couponQuantity.increaseIssuedQuantity();

        userCouponRepository.save(UserCoupon.create(userId, couponId, coupon.getIssuedEndedAt()));
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
