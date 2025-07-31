CREATE TABLE coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    discount_type VARCHAR(20),
    discount_value INT,
    issued_started_at DATETIME,
    issued_ended_at DATETIME,
    created_at DATETIME
);

CREATE TABLE coupon_quantity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_id BIGINT NOT NULL,
    total_quantity INT,
    issued_quantity INT,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    issued_at DATETIME,
    expired_at DATETIME,
    created_at DATETIME
);

CREATE TABLE user_coupon_state (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_coupon_id BIGINT NOT NULL,
    user_coupon_status VARCHAR(20),
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE user_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    balance INT,
    updated_at DATETIME
);

CREATE TABLE point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_point_id BIGINT NOT NULL,
    order_id BIGINT,
    amount INT,
    transaction_type VARCHAR(20),
    created_at DATETIME
);

CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255),
    price_per_unit INT,
    quantity INT,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE order_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    user_coupon_id BIGINT NOT NULL,
    discount_amount INT,
    created_at DATETIME
);

CREATE TABLE order_payment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    order_amount INT,
    discount_amount INT,
    payment_amount INT,
    created_at DATETIME
);

CREATE TABLE order_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT,
    created_at DATETIME
);

CREATE TABLE product_daily_sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sales_date DATE NOT NULL,
    quantity INT NOT NULL
);