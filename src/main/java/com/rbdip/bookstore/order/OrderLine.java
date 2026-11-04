package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;

public record OrderLine(Product product, int quantity) {

    public PricingCalculator.LineItem toPricingLine() {
        return new PricingCalculator.LineItem(product.getPrice(), quantity);
    }

    public OrderItem toOrderItem(Long orderId) {
        return new OrderItem(orderId, product.getName(), product.getPrice(), quantity);
    }
}
