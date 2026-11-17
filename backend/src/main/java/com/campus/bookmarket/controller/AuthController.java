package com.campus.bookmarket.controller;

import com.campus.bookmarket.auth.CurrentUser;
import com.campus.bookmarket.common.ApiResponse;
import com.campus.bookmarket.common.AuthSupport;
import com.campus.bookmarket.dto.AuthDtos;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthDtos.LoginResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.LoginResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<User> me(@CurrentUser User user) {
        User u = AuthSupport.require(user);
        u.setPassword(null);
        return ApiResponse.ok(u);
    }
}
