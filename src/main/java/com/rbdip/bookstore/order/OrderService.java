package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Транзакционный оркестратор создания заказа (ЛР1, SRP). Каждый шаг
 * делегирован классу с единственной ответственностью: валидация -
 * {@link OrderRequestValidator}, сборка доменных объектов -
 * {@link OrderFactory}, расчёт цены - {@link PricingCalculator},
 * уведомление клиента - {@link OrderNotificationService}, запись в БД -
 * репозиториям Spring Data. В самом сервисе остались только порядок
 * шагов и границы транзакции.
 */
@Service
public class OrderService {

    private final OrderRequestValidator requestValidator;
    private final OrderFactory orderFactory;
    private final PricingCalculator pricingCalculator;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderNotificationService notificationService;

    public OrderService(
            OrderRequestValidator requestValidator,
            OrderFactory orderFactory,
            PricingCalculator pricingCalculator,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderNotificationService notificationService) {
        this.requestValidator = requestValidator;
        this.orderFactory = orderFactory;
        this.pricingCalculator = pricingCalculator;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        requestValidator.validate(request);
        List<OrderLine> lines = orderFactory.resolveLines(request);

        Order order = orderRepository.save(orderFactory.createOrder(request));
        for (OrderLine line : lines) {
            orderItemRepository.save(line.toOrderItem(order.getId()));
        }

        BigDecimal total = pricingCalculator.calculateOrderTotal(
                lines.stream().map(OrderLine::toPricingLine).toList(),
                customerTypeOrDefault(request),
                request.couponCode());
        notificationService.sendConfirmationEmail(request.customerFullName(), order.getId(), total);

        return order;
    }

    private String customerTypeOrDefault(CreateOrderRequest request) {
        return request.customerType() == null ? PricingCalculator.CUSTOMER_TYPE_REGULAR : request.customerType();
    }
}
