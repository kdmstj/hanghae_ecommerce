package kr.hhplus.be.server.product.presentation.dto;

public record BestProductResponse(
        long productId,
        String productName,
        int totalSoldQuantity,
        int totalSoldAmount
) {
}
