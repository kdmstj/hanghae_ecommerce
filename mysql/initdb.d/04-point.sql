USE hhplus;

DELIMITER $$

DROP PROCEDURE IF EXISTS seed_user_points $$
CREATE PROCEDURE seed_user_points(
    IN p_max_user_id INT,
    IN p_min_balance INT,
    IN p_max_balance INT
)
BEGIN
  DECLARE i INT DEFAULT 1;

  WHILE i <= p_max_user_id DO
    INSERT INTO user_point (user_id, balance, updated_at, version)
    SELECT i,
           FLOOR(p_min_balance + (RAND() * (p_max_balance - p_min_balance + 1))),
           NOW(),
           0
    FROM DUAL
    WHERE NOT EXISTS (
      SELECT 1 FROM user_point up WHERE up.user_id = i
    );

    SET i = i + 1;
  END WHILE;
END $$

DELIMITER ;