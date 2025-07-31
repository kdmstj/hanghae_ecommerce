package kr.hhplus.be.server.product.application.result;

public record BestProductResult(
        long productId,
        String productName,
        long totalSalesQuantity
) {
}
