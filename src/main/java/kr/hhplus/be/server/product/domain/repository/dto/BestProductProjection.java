package kr.hhplus.be.server.product.domain.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BestProductProjection {
    long productId;
    String productName;
    Long totalSalesQuantity;
}
