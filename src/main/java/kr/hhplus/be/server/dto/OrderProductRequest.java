package kr.hhplus.be.server.dto;

public record OrderProductRequest(
        long productId,
        int quantity
) {

}
