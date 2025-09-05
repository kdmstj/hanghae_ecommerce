package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.repository.CouponIssueCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponIssueRedisRepository implements CouponIssueCacheRepository {

    private static final String COUPON_LIMIT_QUANTITY_PREFIX = "coupon:quantity:limit:";
    private static final String COUPON_ISSUE_PREFIX = "coupon:issue:";
    private static final String COUPON_QUEUE_PREFIX = "coupon:queue:";
    private static final String COUPON_PENDING_PREFIX = "coupon:pending";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void setCouponLimitQuantity(long couponId, int totalQuantity) {
        redisTemplate.opsForValue().setIfAbsent(
                COUPON_LIMIT_QUANTITY_PREFIX + couponId,
                String.valueOf(totalQuantity)
        );
    }

    @Override
    public int getCouponLimitQuantity(long couponId) {
        return Integer.parseInt(redisTemplate.opsForValue().get(COUPON_LIMIT_QUANTITY_PREFIX+ couponId));
    }

    @Override
    public void saveIssuedUser(long couponId, long userId) {
        redisTemplate.opsForSet().add(COUPON_ISSUE_PREFIX + couponId, String.valueOf(userId));
    }

    @Override
    public boolean existsIssuedUser(long couponId, long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(COUPON_ISSUE_PREFIX + couponId, String.valueOf(userId))
        );
    }

    @Override
    public long countIssuedUser(long couponId) {
        return redisTemplate.opsForSet().size(COUPON_ISSUE_PREFIX + couponId);
    }
}
