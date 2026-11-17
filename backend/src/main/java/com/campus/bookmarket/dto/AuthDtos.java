package com.campus.bookmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDtos {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "长度需在 3-20 之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "长度需在 6-32 之间")
        private String password;

        private String nickname;
        private String campus;
        private String phone;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long userId;
        private String username;
        private String nickname;
        private String role;
        private String campus;
        private Integer creditScore;
    }
}
