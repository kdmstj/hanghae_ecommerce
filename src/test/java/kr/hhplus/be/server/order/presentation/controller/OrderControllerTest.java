package kr.hhplus.be.server.order.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.application.facade.OrderFacade;
import kr.hhplus.be.server.order.application.result.OrderPaymentResult;
import kr.hhplus.be.server.order.application.result.OrderProductResult;
import kr.hhplus.be.server.order.application.result.OrderResult;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.presentation.dto.request.OrderCouponRequest;
import kr.hhplus.be.server.order.presentation.dto.request.OrderPaymentRequest;
import kr.hhplus.be.server.order.presentation.dto.request.OrderProductRequest;
import kr.hhplus.be.server.order.presentation.dto.request.OrderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderFacade orderFacade;

    private final String BASE_URI = "/api/v1/users/{userId}/orders";

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @DisplayName("성공")
        void 주문_생성_성공() throws Exception {
            // given
            long userId = 1L;
            long orderId = 100L;

            OrderRequest request = new OrderRequest(
                    new OrderPaymentRequest(10000, 3000, 7000),
                    List.of(new OrderProductRequest(1L, 2)),
                    List.of(new OrderCouponRequest(10L, 3000))
            );

            Order order = Order.builder().id(orderId).userId(userId).build();
            List<OrderProduct> products = List.of(
                    OrderProduct.create(orderId, 1L, 2)
            );
            OrderPayment payment = OrderPayment.create(orderId, 10000, 3000, 7000);

            List<OrderProductResult> productResults = products.stream().map(OrderProductResult::from).toList();
            OrderPaymentResult paymentResult = OrderPaymentResult.from(payment);
            OrderResult result = new OrderResult(orderId, userId, productResults, paymentResult, List.of());

            when(orderFacade.place(userId, request.toCommand(userId))).thenReturn(result);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.put(BASE_URI, userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(orderId))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.products[0].productId").value(1L))
                    .andExpect(jsonPath("$.products[0].quantity").value(2))
                    .andExpect(jsonPath("$.payment.orderAmount").value(10000))
                    .andExpect(jsonPath("$.payment.discountAmount").value(3000))
                    .andExpect(jsonPath("$.payment.paymentAmount").value(7000));
        }
    }
}
