package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponStateRepository;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import kr.hhplus.be.server.coupon.fixture.CouponQuantityFixture;
import kr.hhplus.be.server.coupon.fixture.UserCouponFixture;
import kr.hhplus.be.server.coupon.fixture.UserCouponStateFixture;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class CouponServiceIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserCouponStateRepository userCouponStateRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponQuantityRepository couponQuantityRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    public void setUp() {
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("유효한 보유 쿠폰 목록 조회")
    class GetValidCoupons {


        @Test
        @DisplayName("완료")
        void 유효한_보유_쿠폰_목록_조회() {
            //given
            long userId = 1L;
            for (int i = 1; i <= 10; i++) {
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, i));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));
            }

            UserCoupon usedUserCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 11));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(usedUserCoupon.getId(), UserCouponStatus.USED));

            UserCoupon expiredUserCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, 12));
            userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(expiredUserCoupon.getId(), UserCouponStatus.EXPIRED));


            //when
            List<UserCouponResult> result = couponService.getValidCoupons(userId);

            //then
            assertThat(result).hasSize(10);
            assertThat(result)
                    .extracting(UserCouponResult::couponId)
                    .doesNotContain(11L, 12L);
        }

        @Nested
        @DisplayName("쿠폰 발급")
        class IssueUserCoupon {

            @Test
            @DisplayName("성공")
            void 쿠폰_발급_성공(){
                //given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                int issuedQuantity = 0;
                couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, issuedQuantity));
                long userId = 1L;

                //when
                couponService.issue(userId, coupon.getId());

                //then
                assertThat(couponQuantityRepository.findOneByCouponId(coupon.getId()).getIssuedQuantity()).isEqualTo(issuedQuantity + 1);
                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())).isTrue();
            }

            @Test
            @DisplayName("실패 - 이미 보유한 쿠폰")
            void 쿠폰_발급_실패_이미_보유한_쿠폰(){
                //given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, 1));
                long userId = 1L;
                userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, coupon.getId()));

                // when & then
                assertThatThrownBy(() -> couponService.issue(userId, coupon.getId()))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ALREADY_ISSUED_COUPON.getMessage());
                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())).isTrue();
                assertThat(couponQuantityRepository.findOneByCouponId(coupon.getId()).getIssuedQuantity()).isEqualTo(1);
            }

            @Test
            @DisplayName("실패 - 존재하지 않는 쿠폰")
            void 쿠폰_발급_실패_존재하지_않는_쿠폰(){
                // when & then
                long userId = 1L;
                assertThatThrownBy(() -> couponService.issue(1L, 999L))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.COUPON_NOT_FOUND.getMessage());

                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, 999L)).isFalse();
            }

            @Test
            @DisplayName("실패 - 발급 시작 기간 이전인 쿠폰")
            void 쿠폰_발급_실패_시작_기간이_이전_쿠폰(){
                //given
                Coupon coupon = couponRepository.save(CouponFixture.withIssuedStartedAtAndIssuedEndedAt(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusWeeks(1)));
                couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, 0));
                long userId = 1L;

                // when & then
                assertThatThrownBy(() -> couponService.issue(userId, coupon.getId()))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ISSUE_PERIOD_NOT_STARTED.getMessage());
                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())).isFalse();
                assertThat(couponQuantityRepository.findOneByCouponId(coupon.getId()).getIssuedQuantity()).isEqualTo(0);
            }

            @Test
            @DisplayName("실패 - 발급 기간이 지난 쿠폰")
            void 쿠폰_발급_실패_발급_기간이_지난_쿠폰(){
                //given
                Coupon coupon = couponRepository.save(CouponFixture.withIssuedStartedAtAndIssuedEndedAt(LocalDateTime.now().minusWeeks(1), LocalDateTime.now().minusDays(1)));
                couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), 100, 0));
                long userId = 1L;

                // when & then
                assertThatThrownBy(() -> couponService.issue(userId, coupon.getId()))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ISSUE_PERIOD_ENDED.getMessage());
                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())).isFalse();
                assertThat(couponQuantityRepository.findOneByCouponId(coupon.getId()).getIssuedQuantity()).isEqualTo(0);
            }

            @Test
            @DisplayName("실패 - 수량이 초과한 쿠폰")
            void 쿠포_발급_실패_수량이_초과한_쿠폰(){
                //given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                int totalQuantity = 100; int issuedQuantity = 100;
                couponQuantityRepository.save(CouponQuantityFixture.withCouponIdAndTotalQuantityAndIssuedQuantity(coupon.getId(), totalQuantity, issuedQuantity));
                long userId = 1L;

                // when & then
                assertThatThrownBy(() -> couponService.issue(userId, coupon.getId()))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.EXCEED_QUANTITY.getMessage());
                assertThat(userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())).isFalse();
                assertThat(couponQuantityRepository.findOneByCouponId(coupon.getId()).getIssuedQuantity()).isEqualTo(issuedQuantity);

            }
        }

        @Nested
        @DisplayName("쿠폰 사용")
        class UseUserCoupon {

            @Test
            @DisplayName("성공 - 보유 중인 쿠폰을 사용 상태로 변경")
            void 쿠폰_사용_성공() {
                // given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(1L, coupon.getId()));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));

                CouponUseCommand command = new CouponUseCommand(userCoupon.getId(), 1000);
                List<CouponUseCommand> commands = List.of(command);

                // when
                couponService.use(commands);

                // then
                UserCouponState updated = userCouponStateRepository.findOneByUserCouponId(userCoupon.getId()).orElseThrow();
                assertThat(updated.getUserCouponStatus()).isEqualTo(UserCouponStatus.USED);
            }

            @Test
            @DisplayName("실패 - 존재하지 않는 userCouponId")
            void 쿠폰_사용_실패_없는_ID() {
                // given
                List<CouponUseCommand> commands = List.of(new CouponUseCommand(999L, 1000));

                // when & then
                assertThatThrownBy(() -> couponService.use(commands))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.USER_COUPON_NOT_FOUND.getMessage());
            }

            @Test
            @DisplayName("실패 - 이미 사용된 쿠폰은 다시 사용할 수 없다")
            void 쿠폰_사용_실패_이미_사용됨() {
                // given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(1L, coupon.getId()));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.USED));

                List<CouponUseCommand> commands = List.of(new CouponUseCommand(userCoupon.getId(), 1000));

                // when & then
                assertThatThrownBy(() -> couponService.use(commands))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());
            }

            @Test
            @DisplayName("실패 - 만료된 쿠폰은 사용할 수 없다")
            void 쿠폰_사용_실패_만료됨() {
                // given
                Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
                UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(1L, coupon.getId()));
                userCouponStateRepository.save(UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.EXPIRED));

                List<CouponUseCommand> commands = List.of(new CouponUseCommand(userCoupon.getId(), 1000));

                // when & then
                assertThatThrownBy(() -> couponService.use(commands))
                        .isInstanceOf(BusinessException.class)
                        .hasMessageContaining(ErrorCode.ALREADY_EXPIRED.getMessage());
            }
        }
    }
}
