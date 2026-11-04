package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewControllerTest {

    private final ReviewService reviewService = mock(ReviewService.class);
    private final ReviewController controller = new ReviewController(reviewService);

    @Test
    void addReviewReturnsCreatedId() {
        Review review = new Review(5L, "Olga", 5, "Great book");
        ReflectionTestUtils.setField(review, "id", 9L);
        when(reviewService.addReview(5L, "Olga", 5, "Great book")).thenReturn(review);

        Map<String, Object> response = controller.addReview(
                5L, Map.of("authorName", "Olga", "rating", 5, "comment", "Great book"));

        assertThat(response).containsEntry("id", 9L);
    }

    @Test
    void listReviewsRendersCommentsReplacingNullWithEmptyString() {
        when(reviewService.listReviews(5L)).thenReturn(List.of(
                new Review(5L, "Olga", 5, "Great book"),
                new Review(5L, "Petr", 3, null)));

        List<Map<String, Object>> response = controller.listReviews(5L);

        assertThat(response).hasSize(2);
        assertThat(response.get(0)).containsEntry("authorName", "Olga").containsEntry("comment", "Great book");
        assertThat(response.get(1)).containsEntry("authorName", "Petr").containsEntry("comment", "");
    }
}
