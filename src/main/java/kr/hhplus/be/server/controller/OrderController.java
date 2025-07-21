package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/orders")
public class OrderController {

    @PutMapping
    public ResponseEntity<OrderResponse> order(
            @PathVariable Long userId,
            @RequestBody OrderRequest request
    ) {
        List<OrderProductResponse> products = List.of(
                new OrderProductResponse(
                        1L,
                        request.products().get(0).productId(),
                        10_000,
                        request.products().get(0).quantity(),
                        10_000 * request.products().get(0).quantity()
                )
        );

        OrderPaymentResponse payment = new OrderPaymentResponse(
                1L,
                30_000,
                5_000,
                25_000
        );

        List<OrderCouponResponse> coupons = List.of(
                new OrderCouponResponse(
                        1L,
                        request.couponIds().isEmpty() ? null : request.couponIds().get(0),
                        5_000
                )
        );

        OrderResponse response = new OrderResponse(
                1L,
                userId,
                LocalDateTime.now(),
                products,
                payment,
                coupons
        );

        return ResponseEntity.ok(response);
    }
}
