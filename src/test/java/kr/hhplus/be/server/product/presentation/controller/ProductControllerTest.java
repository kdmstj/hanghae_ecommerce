package kr.hhplus.be.server.product.presentation.controller;

import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.application.result.BestProductResult;
import kr.hhplus.be.server.product.application.result.ProductResult;
import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.fixture.ProductFixture;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    private final String BASE_URI = "/api/v1/products";
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;


    @Nested
    @DisplayName("재고 목록 조회")
    class GetProductList {
        @Test
        @DisplayName("성공")
        void 재고_목록_조회_성공() throws Exception {
            // given
            List<Product> list = ProductFixture.createList(5);
            when(productService.get()).thenReturn(list.stream().map(ProductResult::from).toList());

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(list.get(0).getId()))
                    .andExpect(jsonPath("$[0].productName").value(list.get(0).getProductName()))
                    .andExpect(jsonPath("$[0].pricePerUnit").value(list.get(0).getPricePerUnit()))
                    .andExpect(jsonPath("$[0].quantity").value(list.get(0).getQuantity()))
                    .andExpect(jsonPath("$[1].id").value(list.get(1).getId()));
        }
    }

    @Nested
    @DisplayName("재고 상세 조회")
    class GetProduct {
        @Test
        @DisplayName("성공")
        void 재고_상세_조회_성공() throws Exception {
            // given
            long id = 1L;
            Product product = ProductFixture.withId(id);

            when(productService.get(id)).thenReturn(ProductResult.from(product));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.productName").value(product.getProductName()))
                    .andExpect(jsonPath("$.pricePerUnit").value(product.getPricePerUnit()))
                    .andExpect(jsonPath("$.quantity").value(product.getQuantity()));

        }

        @Test
        @DisplayName("실패 - 존재하는 상품이 없는 경우")
        void 상세_상품_조회_실패() throws Exception {
            // given
            long id = 1L;
            when(productService.get(id)).thenThrow(new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("상위 제품 목록 조회")
    class GetBestProductList {

        @Test
        @DisplayName("성공")
        void 상위_제품_목록_조회_성공() throws Exception {
            // given
            List<BestProductResult> bestProducts = List.of(
                    new BestProductResult(1L, "베스트상품1", 10),
                    new BestProductResult(2L, "베스트상품2", 20)
            );
            when(productService.getBest()).thenReturn(bestProducts);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get(BASE_URI + "/best")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].productName").value("베스트상품1"))
                    .andExpect(jsonPath("$[1].totalSalesQuantity").value(20));
        }
    }
}
