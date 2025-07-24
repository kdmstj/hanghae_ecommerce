```mermaid
---
config:
  theme: redux
  layout: elk
---
erDiagram
    USER {
        BIGINT id PK "사용자 식별자"
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }
    USER ||--|| USER_POINT : owns
    USER ||--o{ USER_COUPON : owns
    USER ||--o{ ORDER : places   
    USER_POINT {
        BIGINT id PK "사용자 쿠폰 식별자"
        BIGINT user_id FK 
        BIGINT balance "포인트 잔액"
        DATETIME updated_at "수정일"
    }
    USER_POINT ||--o{ USER_POINT_HISTORY : has
    USER_POINT_HISTORY {
        BIGINT id PK
        BIGINT user_point_id FK
        BIGINT order_id FK  "사용한 주문에 대한 정보"
        INT amount "충전인 경우 양수, 사용인 경우 음수"
        VARCHAR(10) transaction_type "거래 종류(CHARGE, USE)"
        DATETIME created_at "생성일"
    }
    COUPON {
        BIGINT id PK
        VARCHAR(255) name "쿠폰 이름"
        VARCHAR(10) discount_type "할인종류(PERCENT, FIXED)"
        INT discount_value "할인값"
        INT total_quantity "초기 수량"
        INT issued_quantity "발급된 수량"
        DATETIME issued_started_at "쿠폰 발급 가능 시작"
        DATETIME issued_ended_at  "쿠폰 발급 가능 종료"
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }
    COUPON ||--o{ USER_COUPON : grants
    USER_COUPON {
        BIGINT id PK
        BIGINT user_id FK
        BIGINT coupon_id FK
        BIGINT order_id FK
        INT discount_amount "할인금액"
        DATETIME issued_at "발급 날짜"
        DATETIME expired_at "만료 날짜"
        DATETIME used_at "사용 날짜"
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }
    ORDER {
        BIGINT id PK
        BIGINT user_id FK
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }
    ORDER ||--o{ USER_COUPON : records
    ORDER ||--|| ORDER_PAYMENT : has
    ORDER ||--o{ USER_POINT_HISTORY : records
    ORDER ||--o{ ORDER_PRODUCT : has
    ORDER_PAYMENT {
        BIGINT id PK
        BIGINT order_id FK
        INT order_amount "주문 금액"
        INT discount_amount "할인 금액"
        INT payment_amount "결제 금액"
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }
    
    ORDER_PRODUCT {
        BIGINT id PK
        BIGINT order_id
        BIGINT product_id
        INT quantity
        DATETIME created_at "생성일"
    }
    PRODUCT ||--o{ ORDER_PRODUCT : included_in
    PRODUCT {
        BIGINT id PK
        VARCHAR(255) productName "상품 이름"
        INT price_per_unit "개당 가격"
        INT quantity "수량"
        DATETIME created_at "생성일"
        DATETIME updated_at "수정일"
    }

    BEST_PRODUCT{
        BIGINT product_id "상품 식별자"
        VARHCAR(255) productName "상품 이름"
        INT total_sold_quantity "누적 판매량"
        INT total_sold_amount "누적 판매금"
        DATETIME created_at "생성일"
    }
```