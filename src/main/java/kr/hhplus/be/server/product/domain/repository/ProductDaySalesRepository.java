package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.value.ProductSalesIncrement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ProductDaySalesRepository {

    void incrementProductSalesQuantity(LocalDateTime orderedAt, List<ProductSalesIncrement> products);

    List<SalesProductResult> findTopProducts(int limit, int day);

    List<SalesProductResult> findProductSales(LocalDate day);
}
