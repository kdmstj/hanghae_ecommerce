package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductDailySales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    long productId;

    LocalDate salesDate;

    int quantity;
}
