package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderRequestValidatorTest {

    private final OrderRequestValidator validator = new OrderRequestValidator();

    private CreateOrderRequest request(String fullName, String address, List<CreateOrderRequest.Item> items) {
        return new CreateOrderRequest(fullName, address, null, "regular", null, items);
    }

    @Test
    void validRequestPasses() {
        assertThatCode(() -> validator.validate(
                request("Ivan Petrov", "Moscow", List.of(new CreateOrderRequest.Item(1L, 1)))))
                .doesNotThrowAnyException();
    }

    @Test
    void nullNameIsRejected() {
        assertThatThrownBy(() -> validator.validate(request(null, "Moscow", List.of(new CreateOrderRequest.Item(1L, 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> validator.validate(request("   ", "Moscow", List.of(new CreateOrderRequest.Item(1L, 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void nullAddressIsRejected() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", null, List.of(new CreateOrderRequest.Item(1L, 1)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerAddress is required");
    }

    @Test
    void nullItemsIsRejected() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", "Moscow", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }

    @Test
    void emptyItemsIsRejected() {
        assertThatThrownBy(() -> validator.validate(request("Ivan Petrov", "Moscow", List.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must contain at least one item");
    }
}
