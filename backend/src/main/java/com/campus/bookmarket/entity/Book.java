package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_book", indexes = {
        @Index(name = "idx_book_isbn", columnList = "isbn"),
        @Index(name = "idx_book_category", columnList = "category"),
        @Index(name = "idx_book_status", columnList = "status")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(length = 64)
    private String author;

    @Column(length = 32)
    private String isbn;

    @Column(length = 64)
    private String publisher;

    /** 课程/分类，如“数据结构”“高等数学” */
    @Column(length = 32)
    private String category;

    /** 成色 1-5，5 为全新 */
    private Integer conditionLevel = 4;

    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 500)
    private String description;

    @Column(length = 255)
    private String coverUrl;

    /** ON_SALE / LOCKED / SOLD / OFF_SHELF */
    @Column(nullable = false, length = 16)
    private String status = "ON_SALE";

    private Long sellerId;

    @Column(length = 32)
    private String sellerName;

    @Column(length = 32)
    private String campus;

    private Integer viewCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}
