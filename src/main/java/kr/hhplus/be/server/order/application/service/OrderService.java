package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.order.application.result.OrderAggregate;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductService orderProductService;
    private final OrderPaymentService orderPaymentService;

    public OrderAggregate create(long userId, List<OrderProductCommand> productCommands, PaymentCreateCommand paymentCommand) {
        Order order = orderRepository.save(Order.create(userId));
        long orderId = order.getId();

        List<OrderProduct> orderProducts = orderProductService.create(orderId, productCommands);

        OrderPayment orderPayment = orderPaymentService.create(orderId, paymentCommand);

        return new OrderAggregate(order, orderProducts, orderPayment);
    }
}
