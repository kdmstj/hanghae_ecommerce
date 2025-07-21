package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.CouponResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController {

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> issue(@PathVariable long id) {
        return ResponseEntity.ok(
                new CouponResponse(id, "couponName", "PERCENT", 10, 100, LocalDateTime.now().minusWeeks(1), LocalDateTime.now().plusWeeks(1)));
    }
}
