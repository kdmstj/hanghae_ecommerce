package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.DataBaseCleanUp;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;
import kr.hhplus.be.server.order.application.result.OrderAggregate;
import kr.hhplus.be.server.order.domain.repository.OrderCouponRepository;
import kr.hhplus.be.server.order.domain.repository.OrderPaymentRepository;
import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderPaymentRepository orderPaymentRepository;

    @Autowired
    private OrderCouponRepository orderCouponRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private DataBaseCleanUp dataBaseCleanUp;

    OrderServiceIntegrationTest() {
    }

    @BeforeEach
    void setUp() {
        dataBaseCleanUp.execute();
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {
        @Test
        @DisplayName("OrderService#create - 주문, 상품, 결제, 쿠폰 생성")
        void createOrderAggregate() {
            // given
            long userId = 1L;
            List<OrderProductCommand> productCommands = List.of(new OrderProductCommand(101L, 2), new OrderProductCommand(102L, 3));
            PaymentCreateCommand paymentCommand = new PaymentCreateCommand(10000, 1000, 9000);
            List<CouponUseCommand> couponCommands = List.of(new CouponUseCommand(1L, 1000));

            // when
            OrderAggregate result = orderService.create(userId, productCommands, paymentCommand, couponCommands);

            // then
            assertThat(result.order()).isNotNull();
            assertThat(result.order().getUserId()).isEqualTo(userId);
            assertThat(result.products()).hasSize(2);
            assertThat(result.payment()).isNotNull();
            assertThat(result.payment().getPaymentAmount()).isEqualTo(9000);
            assertThat(result.coupons()).hasSize(1);

            assertThat(orderRepository.findById(result.order().getId())).isPresent();
            assertThat(orderProductRepository.findAll()).hasSize(2);
            assertThat(orderPaymentRepository.findAll()).hasSize(1);
            assertThat(orderCouponRepository.findAll()).hasSize(1);
        }
    }
}

