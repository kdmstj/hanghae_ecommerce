package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.fixture.CouponQuantityFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class CouponQuantityRepositoryTest {

    @Autowired
    private CouponQuantityRepository couponQuantityRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("findOneByCouponId")
    class FindOneByCouponId {

        @Test
        @DisplayName("couponId로 조회 시 대응하는 CouponQuantity를 조회할 수 있다")
        void 성공_조회() {
            // given
            long couponId = 1L;
            CouponQuantity saved = couponQuantityRepository.save(CouponQuantityFixture.withCouponId(couponId));

            // when
            CouponQuantity found = couponQuantityRepository.findOneByCouponId(couponId);

            // then
            assertThat(found).isNotNull();
            assertThat(found.getId()).isEqualTo(saved.getId());
            assertThat(found.getCouponId()).isEqualTo(couponId);
        }

        @Test
        @DisplayName("존재하지 않는 couponId를 조회하면 null이 반환된다")
        void 조회_실패() {
            // when
            CouponQuantity result = couponQuantityRepository.findOneByCouponId(999L);

            // then
            assertThat(result).isNull();
        }
    }
}
