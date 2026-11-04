package com.rbdip.bookstore.order;

import com.rbdip.bookstore.purchase.PurchaseVerifier;
import org.springframework.stereotype.Component;

@Component
public class OrderPurchaseVerifier implements PurchaseVerifier {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseVerifier(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean anyPurchasesExist() {
        return !orderRepository.findAll().isEmpty() && !orderItemRepository.findAll().isEmpty();
    }
}
