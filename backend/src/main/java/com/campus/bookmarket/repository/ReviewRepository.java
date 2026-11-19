package com.campus.bookmarket.repository;

import com.campus.bookmarket.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByToUserIdOrderByCreatedAtDesc(Long toUserId);

    boolean existsByOrderIdAndFromUserId(Long orderId, Long fromUserId);
}
