package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Long fromUserId;

    @Column(length = 32)
    private String fromUserName;

    private Long toUserId;

    /** 1-5 星 */
    private Integer rating;

    @Column(length = 300)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
}
