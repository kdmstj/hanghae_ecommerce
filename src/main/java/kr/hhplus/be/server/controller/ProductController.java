package kr.hhplus.be.server.controller;

import kr.hhplus.be.server.dto.BestProductResponse;
import kr.hhplus.be.server.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<List<ProductResponse>> get() {

        List<ProductResponse> list = listOf(
                new ProductResponse(1L, "productName1", 10000, 10),
                new ProductResponse(2L, "productName2", 10000, 20)
        );

        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(new ProductResponse(id, "productName", 10000, 10));
    }

    @GetMapping("/best")
    public ResponseEntity<List<BestProductResponse>> getBestProduct(){

        List<BestProductResponse> list = listOf(
                new BestProductResponse(1L, "productName1", 10000, 10),
                new BestProductResponse(2L, "productName2", 10000, 20)
        );

        return ResponseEntity.ok().body(list);
    }
}
