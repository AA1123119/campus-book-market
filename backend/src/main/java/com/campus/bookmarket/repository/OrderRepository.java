package com.campus.bookmarket.repository;

import com.campus.bookmarket.entity.BookOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<BookOrder, Long> {

    List<BookOrder> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<BookOrder> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    long countByStatus(String status);

    List<BookOrder> findByStatus(String status);
}
