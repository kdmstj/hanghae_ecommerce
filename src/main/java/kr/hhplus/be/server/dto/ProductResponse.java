package kr.hhplus.be.server.dto;

public record ProductResponse(
        long id,
        String productName,
        int pricePerUnit,
        long quantity
) {
}
