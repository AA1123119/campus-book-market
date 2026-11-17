package com.campus.bookmarket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "t_user", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(length = 32)
    private String nickname;

    /** 校区，用于同校区优先推荐与面交 */
    @Column(length = 32)
    private String campus;

    @Column(length = 20)
    private String phone;

    /** USER / ADMIN */
    @Column(nullable = false, length = 16)
    private String role = "USER";

    /** 信用分，初始 100，违规扣分 */
    private Integer creditScore = 100;

    private LocalDateTime createdAt = LocalDateTime.now();
}
