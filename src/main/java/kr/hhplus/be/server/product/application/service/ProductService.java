package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.order.application.command.OrderProductCommand;
import kr.hhplus.be.server.product.application.result.BestProductResult;
import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductSalesRankingService productSalesRankingService;

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

    private Product findWithPessimisticLock(long id) {
        return productRepository.findWithPessimisticLock(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Cacheable(value = "best-products", key = "'cache:best-products'")
    public List<BestProductResult> findTop5BestProductsFor3Days() {
        return buildBestProducts(5, 3);
    }

    @CachePut(value = "best-products", key = "'cache:best-products'")
    public void refreshTop5BestProductsFor3Days() {
        buildBestProducts(5, 3);
    }

    private List<BestProductResult> buildBestProducts(int limit, int days) {
        List<SalesProductResult> ranking = productSalesRankingService.findTopProducts(limit, days);

        List<Long> ids = ranking.stream().map(SalesProductResult::productId).toList();
        Map<Long, Product> productMap = productRepository.findAllByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return ranking.stream()
                .map(r -> {
                    Product p = productMap.get(r.productId());
                    return new BestProductResult(r.productId(), p.getProductName(), r.quantity());
                })
                .toList();
    }

}
