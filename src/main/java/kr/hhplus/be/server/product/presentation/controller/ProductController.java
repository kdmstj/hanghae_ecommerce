package kr.hhplus.be.server.product.presentation.controller;

import kr.hhplus.be.server.product.presentation.dto.BestProductResponse;
import kr.hhplus.be.server.product.application.service.ProductService;
import kr.hhplus.be.server.product.presentation.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> get() {

        List<ProductResponse> response = ProductResponse.from(productService.get());

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(
            @PathVariable long id
    ) {

        ProductResponse response = ProductResponse.from(productService.get(id));

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/best")
    public ResponseEntity<List<BestProductResponse>> getBestProduct(){

        List<BestProductResponse> response = BestProductResponse.from(productService.findTop5BestProductsFor3Days());

        return ResponseEntity.ok().body(response);
    }
}
