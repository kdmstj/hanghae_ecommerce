package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.application.result.BestProductResult;
import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDailySalesRepository productDailySalesRepository;

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
            Product product = findWithPessimisticLock(command.productId());
            product.decreaseQuantity(command.quantity());
        }
    }

    private Product findWithPessimisticLock(long id){
        return productRepository.findWithPessimisticLock(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Cacheable(value = "best-products", key = "'cache:best-products'")
    public List<BestProductResult> getBest() {
        return productDailySalesRepository.findTop5BestProducts(LocalDate.now().minusDays(3))
                .stream()
                .map(projection -> new BestProductResult(
                        projection.getProductId(),
                        projection.getProductName(),
                        projection.getTotalSalesQuantity()))
                .toList();
    }

}
