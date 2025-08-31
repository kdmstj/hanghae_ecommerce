package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderProductService {

    private final OrderProductRepository orderProductRepository;

    public List<OrderProduct> create(long orderId, List<OrderProductCommand> commands) {
        List<OrderProduct> orderProducts = commands.stream()
                .map(command -> OrderProduct.create(
                        orderId,
                        command.productId(),
                        command.quantity()
                ))
                .toList();

        return orderProductRepository.saveAll(orderProducts);
    }
}
