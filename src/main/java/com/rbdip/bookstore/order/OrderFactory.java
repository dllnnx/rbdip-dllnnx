package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderFactory {

    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderFactory(ProductRepository productRepository, CustomerRepository customerRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findByFullName(request.customerFullName())
                .orElseGet(() -> customerRepository.save(new Customer(
                        request.customerFullName(), request.customerAddress(), request.customerPhone())));
        return new Order(customer, Order.STATUS_NEW);
    }

    public List<OrderLine> resolveLines(CreateOrderRequest request) {
        List<OrderLine> lines = new ArrayList<>();
        for (CreateOrderRequest.Item raw : request.items()) {
            Product product = productRepository.findById(raw.productId())
                    .orElseThrow(() -> new IllegalArgumentException("product " + raw.productId() + " not found"));
            int quantity = raw.quantity() == null ? 1 : raw.quantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            lines.add(new OrderLine(product, quantity));
        }
        return lines;
    }
}
