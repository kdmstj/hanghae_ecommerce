package kr.hhplus.be.server.coupon.domain.repository;

public interface CouponIssueCacheRepository {
    void setCouponLimitQuantity(long couponId, int totalQuantity);
    int getCouponLimitQuantity(long couponId);
    void saveIssuedUser(long couponId, long userId);
    boolean existsIssuedUser(long couponId, long userId);
    long countIssuedUser(long couponId);
}
