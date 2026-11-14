package com.dentalradar.repository;

import com.dentalradar.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    List<Review> findByClinicId(String clinicId);

    Optional<Review> findByClinicAndAuthor(String clinicId, String authorUserId);

    Review save(Review review);
}
