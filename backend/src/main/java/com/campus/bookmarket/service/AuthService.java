package com.campus.bookmarket.service;

import com.campus.bookmarket.auth.TokenUtil;
import com.campus.bookmarket.common.BusinessException;
import com.campus.bookmarket.dto.AuthDtos;
import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public AuthDtos.LoginResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("用户名已被注册");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(hash(req.getPassword()));
        user.setNickname(req.getNickname() == null || req.getNickname().isBlank()
                ? req.getUsername() : req.getNickname());
        user.setCampus(req.getCampus() == null ? "主校区" : req.getCampus());
        user.setPhone(req.getPhone());
        user.setRole("USER");
        user.setCreditScore(100);
        userRepository.save(user);
        return toLoginResponse(user);
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!user.getPassword().equals(hash(req.getPassword()))) {
            throw new BusinessException("密码错误");
        }
        return toLoginResponse(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }

    private AuthDtos.LoginResponse toLoginResponse(User user) {
        AuthDtos.LoginResponse res = new AuthDtos.LoginResponse();
        res.setToken(TokenUtil.create(user.getId()));
        res.setUserId(user.getId());
        res.setUsername(user.getUsername());
        res.setNickname(user.getNickname());
        res.setRole(user.getRole());
        res.setCampus(user.getCampus());
        res.setCreditScore(user.getCreditScore());
        return res;
    }

    /** 课程项目使用加盐 SHA-256，生产环境建议 BCrypt */
    private String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(("cbm$" + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
