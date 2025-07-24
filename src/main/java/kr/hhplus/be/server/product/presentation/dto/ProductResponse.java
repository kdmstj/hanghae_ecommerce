package kr.hhplus.be.server.product.presentation.dto;

import kr.hhplus.be.server.product.domain.entity.Product;

import java.util.List;

public record ProductResponse(
        long id,
        String productName,
        int pricePerUnit,
        int quantity
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getPricePerUnit(),
                product.getQuantity()
        );
    }

    public static List<ProductResponse> from(List<Product> products) {
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }
}
