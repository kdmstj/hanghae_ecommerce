package kr.hhplus.be.server.order.presentation.dto.request;

import kr.hhplus.be.server.order.application.command.CouponUseCommand;
import kr.hhplus.be.server.order.application.command.OrderCreateCommand;
import kr.hhplus.be.server.order.application.command.PaymentCreateCommand;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.point.application.command.PointUseCommand;

import java.util.List;

public record OrderRequest(
        OrderPaymentRequest payment,
        List<OrderProductRequest> products,
        List<OrderCouponRequest> coupons
) {
    public OrderCreateCommand toCommand(long userId){
        PaymentCreateCommand paymentCommand = payment.toCommand();

        List<OrderProductCommand> productCommands = products.stream()
                .map(OrderProductRequest::toCommand)
                .toList();

        List<CouponUseCommand> couponCommands = coupons.stream()
                .map(OrderCouponRequest::toCommand)
                .toList();

        PointUseCommand pointUseCommand = new PointUseCommand(userId, payment.paymentAmount());

        return new OrderCreateCommand(paymentCommand, productCommands, couponCommands, pointUseCommand);
    }

}
