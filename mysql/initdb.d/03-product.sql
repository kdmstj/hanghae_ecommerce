DELIMITER $$

CREATE PROCEDURE generate_products(IN cnt INT)
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= cnt DO
        INSERT INTO product (product_name, price_per_unit, quantity, created_at, updated_at)
        VALUES (
            CONCAT('Product_', i),
            FLOOR(100 + (RAND() * 10000)),
            FLOOR(1 + (RAND() * 1000)),
            NOW(),
            NOW()
        );
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;
