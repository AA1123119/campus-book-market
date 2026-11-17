package com.campus.bookmarket.repository;

import com.campus.bookmarket.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("""
            select b from Book b
            where b.status = 'ON_SALE'
              and (:keyword is null or b.title like concat('%', :keyword, '%')
                   or b.author like concat('%', :keyword, '%') or b.isbn = :keyword)
              and (:category is null or b.category = :category)
              and (:minPrice is null or b.price >= :minPrice)
              and (:maxPrice is null or b.price <= :maxPrice)
              and (:campus is null or b.campus = :campus)
            order by b.createdAt desc
            """)
    List<Book> search(@Param("keyword") String keyword,
                      @Param("category") String category,
                      @Param("minPrice") BigDecimal minPrice,
                      @Param("maxPrice") BigDecimal maxPrice,
                      @Param("campus") String campus);

    List<Book> findTop10ByStatusOrderByViewCountDesc(String status);

    List<Book> findTop10ByCategoryAndStatusOrderByViewCountDesc(String category, String status);

    List<Book> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    long countByStatus(String status);
}
