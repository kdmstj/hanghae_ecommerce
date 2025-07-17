package kr.hhplus.be.server.dto;

public record BestProductResponse(
        long productId,
        String productName,
        int totalSoldQuantity,
        int totalSoldAmount
) {
}
