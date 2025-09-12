CALL seed_coupons(100);
CALL seed_coupon_quantities(500, 5000);
CALL seed_user_coupons(20000, 1000000);
CALL seed_user_coupon_states(0.70, 0.20);
CALL generate_products(1000);
CALL seed_user_points(20000, 0, 1000000);
ANALYZE TABLE coupon, coupon_quantity, user_coupon, user_coupon_state, product;



