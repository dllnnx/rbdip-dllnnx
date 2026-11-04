package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OrderControllerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final OrderController controller = new OrderController(
            orderService, orderRepository, orderItemRepository);

    private Order orderWithId(long id, String fullName) {
        Order order = new Order(new Customer(fullName, null, null), Order.STATUS_NEW);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    @Test
    void createOrderReturnsIdAndStatus() {
        when(orderService.createOrder(any())).thenReturn(orderWithId(11L, "Ivan Petrov"));

        Map<String, Object> response = controller.createOrder(new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1))));

        assertThat(response).containsEntry("id", 11L).containsEntry("status", "new");
    }

    @Test
    void listOrdersReturnsCustomerNameAndItemLines() {
        Order order = orderWithId(3L, "Anna Sidorova");
        Product product = new Product("Refactoring", new BigDecimal("40.00"), null);
        OrderItem item = new OrderItem(3L, product, 2);
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderItemRepository.findByOrderId(3L)).thenReturn(List.of(item));

        List<Map<String, Object>> response = controller.listOrders();

        assertThat(response).hasSize(1);
        Map<String, Object> orderMap = response.get(0);
        assertThat(orderMap.get("customerFullName")).isEqualTo("Anna Sidorova");
        assertThat(orderMap.get("status")).isEqualTo("new");
        List<?> items = (List<?>) orderMap.get("items");
        Map<?, ?> itemMap = (Map<?, ?>) items.get(0);
        assertThat(itemMap.get("productName")).isEqualTo("Refactoring");
        assertThat(itemMap.get("quantity")).isEqualTo(2);
    }
}
