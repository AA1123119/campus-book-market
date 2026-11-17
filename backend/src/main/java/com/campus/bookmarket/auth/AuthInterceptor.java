package com.campus.bookmarket.auth;

import com.campus.bookmarket.entity.User;
import com.campus.bookmarket.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER = "currentUser";

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Long userId = TokenUtil.parse(header.substring(7));
            if (userId != null) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    request.setAttribute(ATTR_USER, user);
                }
            }
        }
        return true;
    }
}
