package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderFactoryTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final CustomerRepository customerRepository = mock(CustomerRepository.class);
    private final OrderFactory factory = new OrderFactory(productRepository, customerRepository);

    private Product product(Long id, String name) {
        Product product = new Product(name, new BigDecimal("10.00"), null);
        org.springframework.test.util.ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Test
    void createOrderReusesExistingCustomerByName() {
        Customer existing = new Customer("Ivan", "Petrov", "Old Address", "+79990000001");
        when(customerRepository.findByFirstNameAndLastName("Ivan", "Petrov")).thenReturn(Optional.of(existing));

        Order order = factory.createOrder(new CreateOrderRequest(
                "Ivan Petrov", "New Address", "+79990000002", "regular", null, List.of()));

        assertThat(order.getCustomer()).isSameAs(existing);
        assertThat(order.getStatus()).isEqualTo("new");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createOrderCreatesCustomerWhenNameIsUnknown() {
        when(customerRepository.findByFirstNameAndLastName("Anna", "Sidorova")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Order order = factory.createOrder(new CreateOrderRequest(
                "Anna Sidorova", "SPb, Nevsky 10", null, "regular", null, List.of()));

        assertThat(order.getCustomer().getFirstName()).isEqualTo("Anna");
        assertThat(order.getCustomer().getLastName()).isEqualTo("Sidorova");
        assertThat(order.getCustomer().getAddress()).isEqualTo("SPb, Nevsky 10");
        assertThat(order.getCustomer().getPhone()).isNull();
    }

    @Test
    void resolveLinesLoadsProductsAndDefaultsNullQuantityToOne() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L, "Clean Code")));

        List<OrderLine> lines = factory.resolveLines(new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(5L, null))));

        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).product().getName()).isEqualTo("Clean Code");
        assertThat(lines.get(0).quantity()).isEqualTo(1);
    }

    @Test
    void resolveLinesRejectsUnknownProduct() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> factory.resolveLines(new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(42L, 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("product 42 not found");
    }

    @Test
    void resolveLinesRejectsZeroQuantity() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L, "Clean Code")));

        assertThatThrownBy(() -> factory.resolveLines(new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(5L, 0)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive");
    }

    @Test
    void resolveLinesRejectsNegativeQuantity() {
        when(productRepository.findById(5L)).thenReturn(Optional.of(product(5L, "Clean Code")));

        assertThatThrownBy(() -> factory.resolveLines(new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(5L, -3)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive");
    }
}
