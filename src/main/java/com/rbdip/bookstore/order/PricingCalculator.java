package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Модуль расчёта цены заказа. ЛР1: magic numbers вынесены в именованные
 * константы, расчет разбит на приватные шаги с одним уровнем абстракции.
 * Порядок арифметических операций сохранён прежним, поэтому результаты
 * совпадают с исходной реализацией.
 */
@Component
public class PricingCalculator {

    public static final String CUSTOMER_TYPE_REGULAR = "regular";
    public static final String CUSTOMER_TYPE_VIP = "vip";
    public static final String CUSTOMER_TYPE_WHOLESALE = "wholesale";

    private static final int BULK_DISCOUNT_THRESHOLD = 10;
    private static final BigDecimal BULK_DISCOUNT_MULTIPLIER = new BigDecimal("0.95");

    private static final BigDecimal VIP_DISCOUNT_MULTIPLIER = new BigDecimal("0.9");
    private static final BigDecimal WHOLESALE_DISCOUNT_MULTIPLIER = new BigDecimal("0.85");

    private static final String SAVE10_COUPON_CODE = "SAVE10";
    private static final BigDecimal SAVE10_COUPON_AMOUNT = BigDecimal.TEN;
    private static final String SAVE20PERCENT_COUPON_CODE = "SAVE20PERCENT";
    private static final BigDecimal SAVE20PERCENT_COUPON_MULTIPLIER = new BigDecimal("0.8");

    private static final BigDecimal LARGE_ORDER_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal LARGE_ORDER_DISCOUNT_MULTIPLIER = new BigDecimal("0.98");

    private static final int PRICE_SCALE = 2;

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = calculateLinesTotal(items);
        total = applyCustomerDiscount(total, customerType);
        total = applyCoupon(total, couponCode);
        total = clampToZeroIfNegative(total);
        total = applyLargeOrderDiscount(total);
        return total.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateLinesTotal(List<LineItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > BULK_DISCOUNT_THRESHOLD) {
                linePrice = linePrice.multiply(BULK_DISCOUNT_MULTIPLIER);
            }
            total = total.add(linePrice);
        }
        return total;
    }

    private BigDecimal applyCustomerDiscount(BigDecimal total, String customerType) {
        if (CUSTOMER_TYPE_VIP.equals(customerType)) {
            return total.multiply(VIP_DISCOUNT_MULTIPLIER);
        }
        if (CUSTOMER_TYPE_WHOLESALE.equals(customerType)) {
            return total.multiply(WHOLESALE_DISCOUNT_MULTIPLIER);
        }
        return total;
    }

    private BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        if (SAVE10_COUPON_CODE.equals(couponCode)) {
            return total.subtract(SAVE10_COUPON_AMOUNT);
        }
        if (SAVE20PERCENT_COUPON_CODE.equals(couponCode)) {
            return total.multiply(SAVE20PERCENT_COUPON_MULTIPLIER);
        }
        return total;
    }

    private BigDecimal clampToZeroIfNegative(BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return total;
    }

    private BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return total.multiply(LARGE_ORDER_DISCOUNT_MULTIPLIER);
        }
        return total;
    }
}
