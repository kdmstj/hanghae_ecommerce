package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;
import kr.hhplus.be.server.order.domain.entity.OrderPayment;
import kr.hhplus.be.server.order.domain.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {

    private final OrderPaymentRepository orderPaymentRepository;

    public OrderPayment create(Long orderId, PaymentCreateCommand command) {

        OrderPayment orderPayment = OrderPayment.create(
                orderId,
                command.orderAmount(),
                command.discountAmount(),
                command.paymentAmount()
        );

        return orderPaymentRepository.save(orderPayment);
    }
}
