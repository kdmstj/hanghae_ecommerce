package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class ProductDailySales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long productId;

    LocalDate salesDate;

    int quantity;

    public static ProductDailySales create(long productId, LocalDate salesDate, int quantity){
        return ProductDailySales.builder()
                .productId(productId)
                .salesDate(salesDate)
                .quantity(quantity)
                .build();
    }
}
