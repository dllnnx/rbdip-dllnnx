package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class OrderServiceTest {

    private final OrderFactory orderFactory = mock(OrderFactory.class);
    private final PricingCalculator pricingCalculator = mock(PricingCalculator.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final OrderNotificationService notificationService = mock(OrderNotificationService.class);
    private final OrderService service = new OrderService(
            new OrderRequestValidator(),
            orderFactory,
            pricingCalculator,
            orderRepository,
            orderItemRepository,
            notificationService);

    private CreateOrderRequest request(String customerType, String coupon) {
        return new CreateOrderRequest(
                "Ivan Petrov", "Moscow, Lenina 1", null, customerType, coupon,
                List.of(new CreateOrderRequest.Item(1L, 2)));
    }

    private Order savedOrder() {
        Order order = new Order(new Customer("Ivan Petrov", "Moscow", null), Order.STATUS_NEW);
        org.springframework.test.util.ReflectionTestUtils.setField(order, "id", 7L);
        return order;
    }

    @Test
    void createOrderPersistsOrderAndItemsThenNotifies() {
        Order order = savedOrder();
        Product product = new Product("Clean Code", new BigDecimal("35.00"), null);
        OrderLine line = new OrderLine(product, 2);
        when(orderFactory.resolveLines(any())).thenReturn(List.of(line));
        when(orderFactory.createOrder(any())).thenReturn(order);
        when(orderRepository.save(any())).thenReturn(order);
        when(pricingCalculator.calculateOrderTotal(any(), any(), any()))
                .thenReturn(new BigDecimal("70.00"));

        Order result = service.createOrder(request("vip", "SAVE10"));

        assertThat(result).isSameAs(order);
        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(orderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getOrderId()).isEqualTo(7L);
        assertThat(itemCaptor.getValue().getProduct()).isSameAs(product);
        assertThat(itemCaptor.getValue().getQuantity()).isEqualTo(2);
        verify(notificationService).sendConfirmationEmail("Ivan Petrov", 7L, new BigDecimal("70.00"));
    }

    @Test
    void nullCustomerTypeFallsBackToRegularForPricing() {
        Order order = savedOrder();
        when(orderFactory.resolveLines(any())).thenReturn(List.of(new OrderLine(new Product("X", new BigDecimal("1.00"), null), 1)));
        when(orderFactory.createOrder(any())).thenReturn(order);
        when(orderRepository.save(any())).thenReturn(order);
        when(pricingCalculator.calculateOrderTotal(any(), any(), any()))
                .thenReturn(new BigDecimal("1.00"));

        service.createOrder(request(null, null));

        ArgumentCaptor<String> customerType = ArgumentCaptor.forClass(String.class);
        verify(pricingCalculator).calculateOrderTotal(any(), customerType.capture(), any());
        assertThat(customerType.getValue()).isEqualTo("regular");
    }

    @Test
    void invalidRequestIsRejectedBeforeAnyPersistence() {
        CreateOrderRequest withoutItems = new CreateOrderRequest(
                "Ivan Petrov", "Moscow, Lenina 1", null, "regular", null, List.of());

        assertThatThrownBy(() -> service.createOrder(withoutItems))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderFactory, never()).resolveLines(any());
        verify(orderFactory, never()).createOrder(any());
        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).save(any());
        verify(notificationService, never()).sendConfirmationEmail(any(), any(), any());
    }
}
