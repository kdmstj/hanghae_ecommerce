package kr.hhplus.be.server.coupon.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.application.result.UserCouponResult;
import kr.hhplus.be.server.coupon.application.service.CouponService;
import kr.hhplus.be.server.coupon.domain.entity.UserCoupon;
import kr.hhplus.be.server.coupon.fixture.UserCouponFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCouponController.class)
public class UserCouponControllerTest {

    private final String BASE_URI = "/api/v1/users/{userId}/coupons";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CouponService couponService;

    @Nested
    @DisplayName("보유 중인 사용자 쿠폰 목록 조회")
    class GetList {

        @Test
        @DisplayName("성공")
        void 보유중인_사용자_쿠폰_목록_조회() throws Exception {
            // given
            long userId = 1l;
            List<UserCoupon> list = UserCouponFixture.createListWithUserId(5, userId);

            when(couponService.getValidCoupons(userId)).thenReturn(list.stream().map(UserCouponResult::from).toList());

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI, userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(list.get(0).getId()))
                    .andExpect(jsonPath("$[0].userId").value(list.get(0).getUserId()))
                    .andExpect(jsonPath("$[0].couponId").value(list.get(0).getCouponId()))
                    .andExpect(jsonPath("$[0].issuedAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
        }
    }

    @Nested
    @DisplayName("쿠폰 발급")
    class IssueUserCoupon {
        @Test
        @DisplayName("성공")
        void 쿠폰_발급_성공() throws Exception {
            // given
            long userId = 1L;
            long couponId = 1L;
            UserCoupon userCoupon = UserCouponFixture.withUserIdAndCouponId(userId, couponId);

            when(couponService.issue(userId, couponId)).thenReturn(UserCouponResult.from(userCoupon));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.put(BASE_URI + "/{couponId}/issue", userId, couponId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userCoupon.getId()))
                    .andExpect(jsonPath("$.userId").value(userCoupon.getUserId()))
                    .andExpect(jsonPath("$.couponId").value(userCoupon.getCouponId()))
                    .andExpect(jsonPath("$.issuedAt").value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T.*")));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 쿠폰인 경우")
        void 존재하지_않는_쿠폰인경우_발급에_실패한다() throws Exception {
            // given
            long userId = 1;
            long couponId = -1;
            when(couponService.issue(userId, couponId)).thenThrow(new BusinessException(ErrorCode.COUPON_NOT_FOUND));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.put(BASE_URI + "/{couponId}/issue", userId, couponId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.COUPON_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("실패 - 이미 발급된 쿠폰인 경우")
        void 이미_발급된_쿠폰인경우_발급에_실패한다() throws Exception {
            // given
            long userId = 1L;
            long couponId = 1L;
            when(couponService.issue(userId, couponId)).thenThrow(new BusinessException(ErrorCode.ALREADY_ISSUED_COUPON));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.put(BASE_URI + "/{couponId}/issue", userId, couponId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_ISSUED_COUPON.getMessage()));
        }
    }
}

