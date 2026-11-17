package com.campus.bookmarket.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** 令牌签名用例：覆盖配置注入、篡改拒绝与密钥轮换。 */
class TokenUtilTest {

    @Test
    void createAndParse() {
        TokenUtil.configure("unit-test-secret");
        assertEquals(42L, TokenUtil.parse(TokenUtil.create(42L)));
    }

    @Test
    void tamperedTokenRejected() {
        TokenUtil.configure("unit-test-secret");
        String token = TokenUtil.create(42L);
        char last = token.charAt(token.length() - 1);
        String forged = token.substring(0, token.length() - 1) + (last == 'A' ? 'B' : 'A');

        assertNull(TokenUtil.parse(forged));
    }

    @Test
    void tokenSignedWithAnotherSecretRejected() {
        TokenUtil.configure("secret-a");
        String token = TokenUtil.create(42L);

        TokenUtil.configure("secret-b");
        assertNull(TokenUtil.parse(token), "换密钥后旧令牌必须失效");

        TokenUtil.configure("dev-only-secret-change-me");
    }
}
