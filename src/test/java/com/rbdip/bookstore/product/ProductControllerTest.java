package com.rbdip.bookstore.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductControllerTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductController controller = new ProductController(productRepository);

    private Product savedProduct(long id, String name, String price) {
        Product product = new Product(name, new BigDecimal(price), null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Test
    void createProductReturnsIdAndName() {
        when(productRepository.save(any()))
                .thenReturn(savedProduct(7L, "Clean Code", "35.00"));

        Map<String, Object> response = controller.createProduct(
                Map.of("name", "Clean Code", "price", "35.00"));

        assertThat(response).containsEntry("id", 7L).containsEntry("name", "Clean Code");
    }

    @Test
    void listProductsReturnsCatalog() {
        when(productRepository.findAll()).thenReturn(List.of(
                savedProduct(1L, "Clean Code", "35.00"),
                savedProduct(2L, "Refactoring", "40.00")));

        List<Map<String, Object>> response = controller.listProducts();

        assertThat(response).hasSize(2);
        assertThat(response.get(0)).containsEntry("id", 1L).containsEntry("price", "35.00");
        assertThat(response.get(1)).containsEntry("id", 2L).containsEntry("name", "Refactoring");
    }
}
