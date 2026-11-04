package com.rbdip.bookstore.review;

import com.rbdip.bookstore.purchase.PurchaseVerifier;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Выделение модуля review (ЛР4, Strangler Fig): вместо прямого доступа
 * к репозиториям order сервис обращается к контракту PurchaseVerifier.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PurchaseVerifier purchaseVerifier;

    public ReviewService(ReviewRepository reviewRepository, PurchaseVerifier purchaseVerifier) {
        this.reviewRepository = reviewRepository;
        this.purchaseVerifier = purchaseVerifier;
    }

    @SuppressWarnings("PMD.UnusedLocalVariable")
    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        boolean verifiedPurchase = purchaseVerifier.anyPurchasesExist();
        Review review = new Review(productId, authorName == null ? "anonymous" : authorName, rating, comment);
        return reviewRepository.save(review);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
