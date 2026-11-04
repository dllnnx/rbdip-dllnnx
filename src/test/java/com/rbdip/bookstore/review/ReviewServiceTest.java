package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.order.OrderItemRepository;
import com.rbdip.bookstore.order.OrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {

    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final ReviewService service = new ReviewService(
            reviewRepository, orderRepository, orderItemRepository);

    @Test
    void addReviewSavesReviewWithGivenAuthor() {
        when(orderRepository.findAll()).thenReturn(List.of());
        when(orderItemRepository.findAll()).thenReturn(List.of());
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Review review = service.addReview(5L, "Olga", 5, "Great book");

        assertThat(review.getProductId()).isEqualTo(5L);
        assertThat(review.getAuthorName()).isEqualTo("Olga");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Great book");
    }

    @Test
    void addReviewFallsBackToAnonymousAuthor() {
        when(orderRepository.findAll()).thenReturn(List.of());
        when(orderItemRepository.findAll()).thenReturn(List.of());
        when(reviewRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.addReview(5L, null, 4, null).getAuthorName()).isEqualTo("anonymous");
    }

    @Test
    void listReviewsDelegatesToRepository() {
        Review review = new Review(5L, "Olga", 5, "Great");
        when(reviewRepository.findByProductId(5L)).thenReturn(List.of(review));

        assertThat(service.listReviews(5L)).containsExactly(review);
        verify(reviewRepository).findByProductId(5L);
    }
}
