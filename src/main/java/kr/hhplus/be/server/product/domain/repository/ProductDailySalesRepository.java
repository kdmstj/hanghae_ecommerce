package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.ProductDailySales;
import kr.hhplus.be.server.product.domain.repository.dto.BestProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductDailySalesRepository extends JpaRepository<ProductDailySales, Long> {
    @Query("""
           SELECT new kr.hhplus.be.server.product.domain.repository.dto.BestProductProjection(
                p.id, p.productName, SUM(pds.quantity))
            FROM ProductDailySales pds
            JOIN Product p ON p.id = pds.productId
            WHERE pds.salesDate >= :from
            GROUP BY p.id
            ORDER BY SUM(pds.quantity) DESC LIMIT 5
            """)
    List<BestProductProjection> findTop5BestProducts(LocalDate from);
}
