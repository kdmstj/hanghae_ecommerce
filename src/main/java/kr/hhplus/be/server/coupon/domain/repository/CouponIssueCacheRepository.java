package kr.hhplus.be.server.coupon.domain.repository;

import java.util.List;
import java.util.Set;

public interface CouponIssueCacheRepository {
    void setCouponLimitQuantity(long couponId, int totalQuantity);
    int getCouponLimitQuantity(long couponId);
    void saveIssuedUser(long couponId, long userId);
    boolean existsIssuedUser(long couponId, long userId);
    long countIssuedUser(long couponId);

    void enqueue(long couponId, long userId);
    Set<Long> popPendingCouponIds(int batchSize);
    List<Long> popPendingCouponUserIds(long couponId, int batchSize);
}
