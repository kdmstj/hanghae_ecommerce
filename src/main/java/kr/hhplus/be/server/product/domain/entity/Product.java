package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String productName;

    int pricePerUnit;

    int quantity;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    public void decreaseQuantity(int requestQuantity){
        if (quantity < requestQuantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_QUANTITY);
        }

        this.updatedAt = LocalDateTime.now();
        this.quantity -= requestQuantity;
    }
}
