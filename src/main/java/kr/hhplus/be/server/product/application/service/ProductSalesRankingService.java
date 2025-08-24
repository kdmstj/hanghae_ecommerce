package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.application.command.SalesProductCommand;
import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.repository.ProductDaySalesRepository;
import kr.hhplus.be.server.product.domain.value.ProductSalesIncrement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSalesRankingService {

    private final ProductDaySalesRepository productDaySalesRepository;

    public void increaseQuantity(LocalDateTime orderedAt, List<SalesProductCommand> salesProducts){
        productDaySalesRepository.incrementProductSalesQuantity(
                orderedAt,
                salesProducts.stream()
                        .map(product -> new ProductSalesIncrement(product.productId(), product.quantity()))
                        .toList());
    }

    public List<SalesProductResult> findTopProducts(int limit, int day) {

        return productDaySalesRepository.findTopProducts(limit, day);
    }
}
