package kr.hhplus.be.server.product.fixture;

import kr.hhplus.be.server.product.domain.entity.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductFixture {
    public static List<Product> createList(int n) {
        List<Product> list = new ArrayList<>();
        for(int i = 0; i < n; i++){
            Product product = withIdAndProductName(i, "product" + i);
            list.add(product);
        }
        return list;
    }

    public static Product withId(long id){
        return Product.builder()
                .id(id)
                .productName("product")
                .pricePerUnit(1000)
                .quantity(100)
                .build();
    }

    public static Product withIdAndProductName(long id, String productName){
        return Product.builder()
                .id(id)
                .productName(productName)
                .pricePerUnit(1000)
                .quantity(100)
                .build();
    }

    public static Product withQuantity(int quantity){
        return Product.builder()
                .id(1L)
                .productName("productName")
                .pricePerUnit(1000)
                .quantity(quantity)
                .build();
    }
}
