package kr.hhplus.be.server.coupon.presentation.controller;

import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueResponse;
import kr.hhplus.be.server.coupon.presentation.dto.UserCouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<UserCouponResponse>> get(
            @PathVariable long userId
    ){
        List<UserCouponResponse> response = UserCouponResponse.from(couponService.getValidCoupons(userId));

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{couponId}/issue")
    public ResponseEntity<CouponIssueResponse> issue(
            @PathVariable long userId,
            @PathVariable long couponId
    ){

        couponService.requestIssue(userId, couponId);
        CouponIssueResponse response = new CouponIssueResponse(couponId, userId);

        return ResponseEntity.ok().body(response);
    }
}
