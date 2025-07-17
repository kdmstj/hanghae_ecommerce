package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.UserCouponResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@RestController
@RequestMapping("/api/v1/users/{userId}/coupons")
public class UserCouponController {

    @GetMapping
    public ResponseEntity<List<UserCouponResponse>> get(
            @PathVariable Long userId
    ){
        List<UserCouponResponse> list = listOf(
          new UserCouponResponse(1L, 1L, "coupon1", "FIX", 1000, LocalDateTime.now(), LocalDateTime.now().plusWeeks(1)),
          new UserCouponResponse(2L, 2L, "coupon2", "PERCENT", 10, LocalDateTime.now(), LocalDateTime.now().plusWeeks(1))
        );
        return ResponseEntity.ok(list);
    }
}
