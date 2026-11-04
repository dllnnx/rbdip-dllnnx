package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OrderNotificationService.class);

    public void sendConfirmationEmail(String customerName, Long orderId, BigDecimal total) {
        log.info("[email] Dear {}, your order #{} for {} has been placed.", customerName, orderId, total);
    }
}
