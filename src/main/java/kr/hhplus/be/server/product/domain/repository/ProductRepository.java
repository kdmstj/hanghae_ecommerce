package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
