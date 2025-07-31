package kr.hhplus.be.server.order.application.facade;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.domain.UserCouponStatus;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.domain.entity.UserCouponState;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.UserCouponStateRepository;
import kr.hhplus.be.server.coupon.fixture.CouponFixture;
import kr.hhplus.be.server.coupon.fixture.UserCouponFixture;
import kr.hhplus.be.server.coupon.fixture.UserCouponStateFixture;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import kr.hhplus.be.server.order.application.command.OrderCreateCommand;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;
import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.domain.repository.OrderCouponRepository;
import kr.hhplus.be.server.order.domain.repository.OrderPaymentRepository;
import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.point.application.command.PointUseCommand;
import kr.hhplus.be.server.point.domain.TransactionType;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.domain.repository.PointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.fixture.ProductFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
public class OrderFacadeIntegrationTest {
    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserCouponStateRepository userCouponStateRepository;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderCouponRepository orderCouponRepository;

    @Autowired
    private OrderPaymentRepository orderPaymentRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    @BeforeEach
    void setUp(){
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("주문")
    class PlaceOrder {

        @Test
        @DisplayName("주문 성공 - 상품 재고 차감, 쿠폰 상태 변경, 포인트 차감 및 이력 기록")
        void 주문_성공() {
            // given
            long userId = 1L;
            int productStock = 100;
            int orderQuantity = 10;
            int userPointBalance = 200_000;
            int pointUseAmount = 90_000;

            Product product = productRepository.save(
                    ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품", 10_000, productStock));

            Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
            UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, coupon.getId()));
            userCouponStateRepository.save(
                    UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));

            UserPoint userPoint = userPointRepository.save(
                    UserPointFixture.withUserIdAndBalance(userId, userPointBalance));

            OrderCreateCommand command = new OrderCreateCommand(
                    new PaymentCreateCommand(100_000, 10_000, 90_000),
                    List.of(new OrderProductCommand(product.getId(), orderQuantity)),
                    List.of(new CouponUseCommand(userCoupon.getId(), 1000)),
                    new PointUseCommand(userId, 90_000)
            );

            // when
            OrderResult result = orderFacade.place(userId, command);

            // then
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantity()).isEqualTo(productStock - orderQuantity);

            UserCouponState state = userCouponStateRepository.findOneByUserCouponId(userCoupon.getId()).orElseThrow();
            assertThat(state.getUserCouponStatus()).isEqualTo(UserCouponStatus.USED);

            assertThat(userPointRepository.findOneByUserId(userId).get().getBalance()).isEqualTo(userPointBalance - pointUseAmount);
            assertThat(pointHistoryRepository.findAll())
                    .hasSize(1)
                    .allSatisfy(history -> {
                        assertThat(history.getUserPointId()).isEqualTo(userPoint.getUserId());
                        assertThat(history.getAmount()).isEqualTo(-pointUseAmount);
                        assertThat(history.getTransactionType()).isEqualTo(TransactionType.USE);
                    });

            assertThat(orderRepository.findById(result.id())).isPresent();
        }

        @Test
        @DisplayName("주문 실패 시 전체 트랜잭션 롤백")
        void 주문_실패_롤백() {
            // given
            long userId = 1L;
            int productStock = 100;
            int orderQuantity = 10;
            int userPointBalance = 1_000;
            int pointUseAmount = 90_000;

            Product product = productRepository.save(
                    ProductFixture.withProductNameAndPricePerUnitAndQuantity("상품", 10_000, productStock));

            Coupon coupon = couponRepository.save(CouponFixture.validPeriod());
            UserCoupon userCoupon = userCouponRepository.save(UserCouponFixture.withUserIdAndCouponId(userId, coupon.getId()));
            userCouponStateRepository.save(
                    UserCouponStateFixture.withUserCouponIdAndWithUserCouponStatus(userCoupon.getId(), UserCouponStatus.ISSUED));

            userPointRepository.save(
                    UserPointFixture.withUserIdAndBalance(userId, userPointBalance));

            OrderCreateCommand command = new OrderCreateCommand(
                    new PaymentCreateCommand(100_000, 10_000, pointUseAmount),
                    List.of(new OrderProductCommand(product.getId(), orderQuantity)),
                    List.of(new CouponUseCommand(userCoupon.getId(), 1000)),
                    new PointUseCommand(userId, pointUseAmount)
            );

            // when
            assertThatThrownBy(() -> orderFacade.place(userId, command))
                    .isInstanceOf(RuntimeException.class);

            // then
            assertThat(orderRepository.findAll()).isEmpty();
            assertThat(orderProductRepository.findAll()).isEmpty();
            assertThat(orderPaymentRepository.findAll()).isEmpty();
            assertThat(orderCouponRepository.findAll()).isEmpty();

            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantity()).isEqualTo(productStock);

            UserCouponState state = userCouponStateRepository.findOneByUserCouponId(userCoupon.getId()).orElseThrow();
            assertThat(state.getUserCouponStatus()).isEqualTo(UserCouponStatus.ISSUED);

            assertThat(pointHistoryRepository.findAll()).isEmpty();
            assertThat(userPointRepository.findOneByUserId(userId).get().getBalance()).isEqualTo(userPointBalance);
        }

    }

}
