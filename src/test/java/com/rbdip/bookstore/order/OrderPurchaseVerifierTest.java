package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPurchaseVerifierTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final OrderPurchaseVerifier verifier = new OrderPurchaseVerifier(orderRepository, orderItemRepository);

    private Order order() {
        return new Order(new Customer("Ivan", "Petrov", null, null), Order.STATUS_NEW);
    }

    @Test
    void returnsTrueWhenOrdersAndItemsExist() {
        when(orderRepository.findAll()).thenReturn(List.of(order()));
        when(orderItemRepository.findAll()).thenReturn(List.of(
                new OrderItem(1L, new Product("X", new BigDecimal("1.00"), null), 1)));

        assertThat(verifier.anyPurchasesExist()).isTrue();
    }

    @Test
    void returnsFalseWhenNoOrders() {
        when(orderRepository.findAll()).thenReturn(List.of());

        assertThat(verifier.anyPurchasesExist()).isFalse();
    }

    @Test
    void returnsFalseWhenOrdersHaveNoItems() {
        when(orderRepository.findAll()).thenReturn(List.of(order()));
        when(orderItemRepository.findAll()).thenReturn(List.of());

        assertThat(verifier.anyPurchasesExist()).isFalse();
    }
}
