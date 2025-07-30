package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.common.BusinessException;
import kr.hhplus.be.server.common.ErrorCode;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResult> get() {

        return productRepository.findAll()
                .stream()
                .map(ProductResult::from)
                .toList();
    }

    public ProductResult get(long id) {

        return ProductResult.from(find(id));
    }

    private Product find(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional
    public void decreaseQuantity(List<OrderProductCommand> commands) {

        for (OrderProductCommand command : commands) {
            Product product = find(command.productId());
            product.decreaseQuantity(command.quantity());
        }
    }

}
