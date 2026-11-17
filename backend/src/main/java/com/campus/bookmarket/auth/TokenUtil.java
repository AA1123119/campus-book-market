package com.campus.bookmarket.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 轻量级令牌工具：token = base64(userId.expire.sign)
 * 说明：课程项目未引入 Spring Security，用 HMAC 签名防止伪造，生产环境应替换为 JWT + 刷新令牌。
 */
public final class TokenUtil {

    private static final String SECRET = "campus-book-market-secret-2026";
    private static final long TTL = 7L * 24 * 60 * 60 * 1000;

    private TokenUtil() {
    }

    public static String create(Long userId) {
        long expire = System.currentTimeMillis() + TTL;
        String raw = userId + "." + expire;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((raw + "." + sign(raw)).getBytes(StandardCharsets.UTF_8));
    }

    /** @return 用户 id，非法或过期返回 null */
    public static Long parse(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                return null;
            }
            if (Long.parseLong(parts[1]) < System.currentTimeMillis()) {
                return null;
            }
            return Long.parseLong(parts[0]);
        } catch (Exception e) {
            return null;
        }
    }

    private static String sign(String raw) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
