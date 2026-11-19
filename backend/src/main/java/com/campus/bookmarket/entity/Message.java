package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_message", indexes = @Index(name = "idx_msg_pair", columnList = "fromUserId,toUserId"))
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private Long bookId;

    @Column(length = 500, nullable = false)
    private String content;

    private Boolean readFlag = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
