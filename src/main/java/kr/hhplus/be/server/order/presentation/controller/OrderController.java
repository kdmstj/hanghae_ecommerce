package kr.hhplus.be.server.order.presentation.controller;

import kr.hhplus.be.server.order.application.facade.OrderFacade;
import kr.hhplus.be.server.order.presentation.dto.request.OrderRequest;
import kr.hhplus.be.server.order.presentation.dto.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderFacade orderFacade;

    @PutMapping
    public ResponseEntity<OrderResponse> order(
            @PathVariable Long userId,
            @RequestBody OrderRequest request
    ) {

        OrderResponse response = OrderResponse.from(orderFacade.place(userId, request.toCommand(userId)));

        return ResponseEntity.ok(response);
    }
}
