package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_order", indexes = {
        @Index(name = "idx_order_buyer", columnList = "buyerId"),
        @Index(name = "idx_order_seller", columnList = "sellerId")
})
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    private String orderNo;

    @Column(nullable = false)
    private Long bookId;

    @Column(length = 128)
    private String bookTitle;

    @Column(nullable = false)
    private Long buyerId;

    @Column(length = 32)
    private String buyerName;

    @Column(nullable = false)
    private Long sellerId;

    @Column(length = 32)
    private String sellerName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** SELF_PICKUP 自提点 / FACE_TO_FACE 面交 */
    @Column(length = 16)
    private String tradeType = "FACE_TO_FACE";

    @Column(length = 64)
    private String tradePlace;

    /** PENDING 待确认 / CONFIRMED 已确认 / COMPLETED 已完成 / CANCELLED 已取消 */
    @Column(nullable = false, length = 16)
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime finishedAt;
}
