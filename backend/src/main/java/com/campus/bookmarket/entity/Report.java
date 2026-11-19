package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** BOOK / USER */
    @Column(length = 16)
    private String targetType;

    private Long targetId;

    @Column(length = 128)
    private String targetTitle;

    private Long reporterId;

    @Column(length = 32)
    private String reporterName;

    @Column(length = 300)
    private String reason;

    /** PENDING / APPROVED / REJECTED */
    @Column(length = 16)
    private String status = "PENDING";

    @Column(length = 300)
    private String handleRemark;

    private LocalDateTime createdAt = LocalDateTime.now();
}
