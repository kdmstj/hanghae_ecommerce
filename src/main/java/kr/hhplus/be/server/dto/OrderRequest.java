package kr.hhplus.be.server.dto;

import java.util.List;

public record OrderRequest(
        List<OrderProductRequest> products,
        List<Long> couponIds
) {
}
