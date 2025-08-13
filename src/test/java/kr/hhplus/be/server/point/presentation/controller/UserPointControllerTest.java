package kr.hhplus.be.server.point.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.point.application.result.UserPointResult;
import kr.hhplus.be.server.point.application.service.PointService;
import kr.hhplus.be.server.point.domain.entity.UserPoint;
import kr.hhplus.be.server.point.fixture.UserPointFixture;
import kr.hhplus.be.server.point.presentation.dto.UserPointRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserPointController.class)
public class UserPointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PointService pointService;

    private final String BASE_URI = "/api/v1/users";

    @Nested
    @DisplayName("포인트 조회")
    class GetUserPoint {

        @Test
        @DisplayName("성공")
        void 포인트_조회_성공() throws Exception {
            //given
            long userId = 1L;
            int balance = 10_000;
            UserPoint userPoint = UserPointFixture.withUserIdAndBalance(userId, balance);

            when(pointService.get(userId)).thenReturn(UserPointResult.from(userPoint));

            //when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/{userId}/points", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userPoint.getId()))
                    .andExpect(jsonPath("$.userId").value(userPoint.getUserId()))
                    .andExpect(jsonPath("$.balance").value(userPoint.getBalance()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자인경우 실패한다.")
        void 포인트_조회_실패() throws Exception {
            //given
            long userId = -1;

            when(pointService.get(userId)).thenThrow(new BusinessException(ErrorCode.USER_POINT_NOT_FOUND));

            //when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/{userId}/points", userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.USER_POINT_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("포인트 충전")
    class Charge {

        @ParameterizedTest
        @DisplayName("성공")
        @ValueSource(ints = {10_000, 11_000})
        void 포인트_충전_성공(int chargeAmount) throws Exception {
            // given
            long userId = 1L;
            int originAmount = 50_000;
            UserPoint userPoint = UserPointFixture.withUserIdAndBalance(userId, originAmount + chargeAmount);

            UserPointRequest request = new UserPointRequest(chargeAmount);
            when(pointService.charge(request.toCommand(userId))).thenReturn(UserPointResult.from(userPoint));

            //when & then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URI + "/{userId}/points/charge", userId)
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.balance").value(originAmount + chargeAmount));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 9_999, 9_900, 9_000})
        @DisplayName("실패 - 충전 금액은 최소 10_000원 이상이어야 한다.")
        void 충전_실패_최소_금액_미만(int chargeAmount) throws Exception {
            //given
            long userId = 1L;
            int originAmount = 50_000;
            UserPoint userPoint = UserPointFixture.withUserIdAndBalance(userId, originAmount);

            //when
            UserPointRequest request = new UserPointRequest(chargeAmount);

            //then
            mockMvc.perform(MockMvcRequestBuilders.patch(BASE_URI + "/{userId}/points/charge", userId)
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("must be greater than or equal to 10000"));

        }
    }
}
