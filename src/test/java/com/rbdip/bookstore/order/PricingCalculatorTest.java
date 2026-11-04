package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.order.PricingCalculator.LineItem;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    private BigDecimal total(String customerType, String coupon, LineItem... items) {
        return calculator.calculateOrderTotal(List.of(items), customerType, coupon);
    }

    @Test
    void regularCustomerWithoutCouponPaysLineTotal() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("10.00"), 2)))
                .isEqualByComparingTo("20.00");
    }

    @Test
    void vipCustomerGetsTenPercentOff() {
        assertThat(total("vip", null, new LineItem(new BigDecimal("100.00"), 1)))
                .isEqualByComparingTo("90.00");
    }

    @Test
    void wholesaleCustomerGetsFifteenPercentOff() {
        assertThat(total("wholesale", null, new LineItem(new BigDecimal("200.00"), 1)))
                .isEqualByComparingTo("170.00");
    }

    @Test
    void unknownCustomerTypeGetsNoDiscount() {
        assertThat(total("student", null, new LineItem(new BigDecimal("100.00"), 1)))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void nullCustomerTypeGetsNoDiscount() {
        assertThat(total(null, null, new LineItem(new BigDecimal("100.00"), 1)))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void customerTypeIsCaseSensitive() {
        assertThat(total("VIP", null, new LineItem(new BigDecimal("100.00"), 1)))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void bulkDiscountStartsAboveTenUnits() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("10.00"), 11)))
                .isEqualByComparingTo("104.50");
    }

    @Test
    void tenUnitsGetNoBulkDiscount() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("10.00"), 10)))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void bulkDiscountIsAppliedPerLineNotPerOrder() {
        assertThat(total("regular", null,
                new LineItem(new BigDecimal("10.00"), 11),
                new LineItem(new BigDecimal("10.00"), 1)))
                .isEqualByComparingTo("114.50");
    }

    @Test
    void bulkDiscountComposesWithVip() {
        assertThat(total("vip", null, new LineItem(new BigDecimal("10.00"), 11)))
                .isEqualByComparingTo("94.05");
    }

    @Test
    void save10CouponSubtractsTenRubles() {
        assertThat(total("regular", "SAVE10", new LineItem(new BigDecimal("60.00"), 1)))
                .isEqualByComparingTo("50.00");
    }

    @Test
    void save20PercentCouponMultipliesByEighty() {
        assertThat(total("regular", "SAVE20PERCENT", new LineItem(new BigDecimal("60.00"), 1)))
                .isEqualByComparingTo("48.00");
    }

    @Test
    void unknownCouponIsIgnored() {
        assertThat(total("regular", "BLACKFRIDAY", new LineItem(new BigDecimal("60.00"), 1)))
                .isEqualByComparingTo("60.00");
    }

    @Test
    void couponCodeIsCaseSensitive() {
        assertThat(total("regular", "save10", new LineItem(new BigDecimal("60.00"), 1)))
                .isEqualByComparingTo("60.00");
    }

    // 105 * 0.9 - 10 = 84.50; при обратном порядке было бы (105 - 10) * 0.9 = 85.50.
    @Test
    void customerDiscountIsAppliedBeforeFixedCoupon() {
        assertThat(total("vip", "SAVE10", new LineItem(new BigDecimal("105.00"), 1)))
                .isEqualByComparingTo("84.50");
    }

    @Test
    void percentCouponComposesWithWholesale() {
        assertThat(total("wholesale", "SAVE20PERCENT", new LineItem(new BigDecimal("200.00"), 1)))
                .isEqualByComparingTo("136.00");
    }

    @Test
    void negativeTotalIsClampedToZero() {
        assertThat(total("regular", "SAVE10", new LineItem(new BigDecimal("5.00"), 1)))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void totalIsZeroWhenCouponConsumesWholeAmount() {
        assertThat(total("regular", "SAVE10", new LineItem(new BigDecimal("10.00"), 1)))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void clampHappensAfterCustomerDiscountAndCoupon() {
        assertThat(total("vip", "SAVE10", new LineItem(new BigDecimal("5.00"), 1)))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void largeOrderGetsTwoPercentOff() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("1500.00"), 1)))
                .isEqualByComparingTo("1470.00");
    }

    @Test
    void exactlyThousandGetsNoLargeOrderDiscount() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("1000.00"), 1)))
                .isEqualByComparingTo("1000.00");
    }

    // 1500 - 10 = 1490; 1490 * 0.98 = 1460.20 - купон применяется раньше
    // скидки за объём закупки.
    @Test
    void largeOrderDiscountIsAppliedAfterCoupon() {
        assertThat(total("regular", "SAVE10", new LineItem(new BigDecimal("1500.00"), 1)))
                .isEqualByComparingTo("1460.20");
    }

    @Test
    void bulkDiscountCountsTowardLargeOrderThreshold() {
        // 11 * 100 = 1100; после скидки за объём 1045 > 1000 -> ещё 2%.
        assertThat(total("regular", null, new LineItem(new BigDecimal("100.00"), 11)))
                .isEqualByComparingTo("1024.10");
    }

    // 2 * 1.005 = 2.010 -> 2.01. Если бы округление делалось по строкам,
    // было бы 1.01 + 1.01 = 2.02 - тест фиксирует однократный итоговый
    // setScale.
    @Test
    void roundingHappensOnceAtTheEndNotPerLine() {
        assertThat(total("regular", null,
                new LineItem(new BigDecimal("1.005"), 1),
                new LineItem(new BigDecimal("1.005"), 1)))
                .isEqualByComparingTo("2.01");
    }

    // 21 * 1.01 = 21.21; после скидки за объём 20.1495 -> 20.15 (HALF_UP).
    @Test
    void resultIsRoundedHalfUpToTwoDecimals() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("1.01"), 21)))
                .isEqualByComparingTo("20.15");
    }

    @Test
    void emptyOrderCostsZero() {
        assertThat(total("regular", "SAVE10"))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void zeroQuantityLineCostsZero() {
        assertThat(total("regular", null, new LineItem(new BigDecimal("100.00"), 0)))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void resultAlwaysHasTwoDecimalScale() {
        BigDecimal total = total("regular", null, new LineItem(new BigDecimal("2.00"), 1));
        assertThat(total).isEqualByComparingTo("2.00");
        assertThat(total.scale()).isEqualTo(2);
    }
}
