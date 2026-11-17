package com.campus.bookmarket.common;

import com.campus.bookmarket.entity.User;

public final class AuthSupport {

    private AuthSupport() {
    }

    public static User require(User user) {
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }
}
