package kr.hhplus.be.server.product.application.service;

import kr.hhplus.be.server.product.application.result.SalesProductResult;
import kr.hhplus.be.server.product.domain.entity.ProductDailySales;
import kr.hhplus.be.server.product.domain.repository.ProductDailySalesRepository;
import kr.hhplus.be.server.product.domain.repository.ProductDaySalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDailySalesService {

    private final ProductDaySalesRepository productDaySalesRepository;
    private final ProductDailySalesRepository productDailySalesRepository;


    public void create() {
        LocalDate targetDate = LocalDate.now().minusDays(1);

        List<SalesProductResult> products = productDaySalesRepository.findProductSales(targetDate);

        if (products.isEmpty()) {
            return;
        }

        List<ProductDailySales> rows = products.stream()
                .map(p -> ProductDailySales.create(p.productId(), targetDate, p.quantity()))
                .toList();

        productDailySalesRepository.saveAll(rows);
    }
}
