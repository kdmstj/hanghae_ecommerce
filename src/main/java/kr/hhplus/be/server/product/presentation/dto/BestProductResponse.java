package kr.hhplus.be.server.product.presentation.dto;

import kr.hhplus.be.server.product.application.result.BestProductResult;

import java.util.List;

public record BestProductResponse(
        long productId,
        String productName,
        long totalSalesQuantity
) {
    public static BestProductResponse from(BestProductResult result){
        return new BestProductResponse(
                result.productId(),
                result.productName(),
                result.totalSalesQuantity()
        );
    }

    public static List<BestProductResponse> from(List<BestProductResult> resultList){
        return resultList.stream()
                .map(BestProductResponse::from)
                .toList();
    }
}
