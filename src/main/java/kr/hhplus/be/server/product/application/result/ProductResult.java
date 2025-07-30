package kr.hhplus.be.server.product.application.result;

import kr.hhplus.be.server.product.domain.entity.Product;

public record ProductResult(
        long id,
        String productName,
        int pricePerUnit,
        int quantity
) {
    public static ProductResult from(Product product){
        return new ProductResult(
                product.getId(),
                product.getProductName(),
                product.getPricePerUnit(),
                product.getQuantity()
        );
    }
}
