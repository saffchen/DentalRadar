package com.dentalradar.repository;

import com.dentalradar.model.Review;
import com.dentalradar.storage.JsonFileStore;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JsonReviewRepository implements ReviewRepository {

    private final JsonFileStore store;

    public JsonReviewRepository(JsonFileStore store) {
        this.store = store;
    }

    @Override
    public List<Review> findByClinicId(String clinicId) {
        return store.read().getReviews().stream()
                .filter(review -> clinicId.equals(review.getClinicId()))
                .toList();
    }

    @Override
    public Optional<Review> findByClinicAndAuthor(String clinicId, String authorUserId) {
        return store.read().getReviews().stream()
                .filter(review -> clinicId.equals(review.getClinicId()))
                .filter(review -> authorUserId.equals(review.getAuthorUserId()))
                .findFirst();
    }

    @Override
    public Review save(Review review) {
        store.update(database -> {
            database.getReviews().removeIf(existing -> existing.getId().equals(review.getId()));
            database.getReviews().add(review);
        });
        return review;
    }
}
