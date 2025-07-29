package kr.hhplus.be.server.product.presentation.dto;

import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.domain.entity.Product;

import java.util.List;

public record ProductResponse(
        long id,
        String productName,
        int pricePerUnit,
        int quantity
) {
    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(
                result.id(),
                result.productName(),
                result.pricePerUnit(),
                result.quantity()
        );
    }

    public static List<ProductResponse> from(List<ProductResult> resultList) {
        return resultList.stream()
                .map(ProductResponse::from)
                .toList();
    }
}
