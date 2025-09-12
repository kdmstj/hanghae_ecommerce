USE hhplus;

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_coupons $$
CREATE PROCEDURE seed_coupons(IN p_count INT)
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= p_count DO
    INSERT INTO coupon (
      name, discount_type, discount_value,
      issued_started_at, issued_ended_at, created_at
    ) VALUES (
      CONCAT('coupon_', i),
      IF(MOD(i, 2) = 0, 'RATE', 'AMOUNT'),
      500 + (i * 10),
      NOW() - INTERVAL 1 DAY,
      NOW() + INTERVAL (10 + MOD(i, 31)) DAY,
      NOW()
    );
    SET i = i + 1;
  END WHILE;
END $$

DROP PROCEDURE IF EXISTS seed_coupon_quantities $$
CREATE PROCEDURE seed_coupon_quantities(IN p_total_min INT, IN p_total_max INT)
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE v_coupon_id BIGINT;
  DECLARE v_exists INT DEFAULT 0;

  DECLARE cur CURSOR FOR
    SELECT id FROM coupon ORDER BY id;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO v_coupon_id;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;

    SELECT COUNT(*) INTO v_exists
    FROM coupon_quantity
    WHERE coupon_id = v_coupon_id;

    IF v_exists = 0 THEN
      INSERT INTO coupon_quantity (
        coupon_id, total_quantity, issued_quantity, created_at, updated_at
      ) VALUES (
        v_coupon_id,
        FLOOR(p_total_min + (RAND() * (p_total_max - p_total_min + 1))),
        0,
        NOW(), NOW()
      );
    END IF;
  END LOOP;
  CLOSE cur;
END $$

DROP PROCEDURE IF EXISTS seed_user_coupons $$
CREATE PROCEDURE seed_user_coupons(IN p_rows INT, IN p_max_user_id INT)
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_coupon_id BIGINT;

  WHILE i <= p_rows DO
    SELECT id INTO v_coupon_id
    FROM coupon
    ORDER BY RAND()
    LIMIT 1;

    INSERT INTO user_coupon (
      user_id, coupon_id, issued_at, expired_at, created_at
    )
    VALUES (
      FLOOR(1 + RAND() * p_max_user_id),
      v_coupon_id,
      NOW() - INTERVAL FLOOR(RAND() * 10) DAY,
      CASE WHEN RAND() < 0.7
           THEN NOW() + INTERVAL FLOOR(1 + RAND() * 30) DAY
           ELSE NOW() - INTERVAL FLOOR(1 + RAND() * 5) DAY
      END,
      NOW()
    );

    SET i = i + 1;
  END WHILE;
END $$

DROP PROCEDURE IF EXISTS seed_user_coupon_states $$
CREATE PROCEDURE seed_user_coupon_states(IN p_valid_ratio DECIMAL(5,4), IN p_used_ratio DECIMAL(5,4))
BEGIN
  /*
    p_valid_ratio + p_used_ratio <= 1.0, 나머지는 EXPIRED
  */
  INSERT INTO user_coupon_state (user_coupon_id, user_coupon_status, created_at, updated_at, version)
  SELECT
    uc.id,
    CASE
      WHEN (@r := RAND()) < p_valid_ratio THEN 'VALID'
      WHEN @r < (p_valid_ratio + p_used_ratio) THEN 'USED'
      ELSE 'EXPIRED'
    END AS status,
    NOW(), NOW(), 1
  FROM user_coupon uc
  LEFT JOIN user_coupon_state s ON s.user_coupon_id = uc.id
  WHERE s.id IS NULL;
END $$

