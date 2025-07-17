package kr.hhplus.be.server.dto;

public record OrderProductResponse(
        long id,
        long productId,
        int pricePerUnit,
        int quantity,
        int totalPrice
) {
}
