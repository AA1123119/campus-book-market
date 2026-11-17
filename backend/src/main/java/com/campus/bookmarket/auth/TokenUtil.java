package com.campus.bookmarket.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 轻量级令牌工具：token = base64(userId.expire.sign)
 * 说明：课程项目未引入 Spring Security，用 HMAC 签名防止伪造，生产环境应替换为 JWT + 刷新令牌。
 */
public final class TokenUtil {

    /** 仅供本地开发；生产通过 app.token.secret（或 APP_TOKEN_SECRET 环境变量）注入，见 WebConfig */
    private static final String DEFAULT_SECRET = "dev-only-secret-change-me";

    private static volatile String secret = DEFAULT_SECRET;

    private static final long TTL = 7L * 24 * 60 * 60 * 1000;

    private TokenUtil() {
    }

    /** 启动时由 WebConfig 注入配置中的密钥；为空则保留开发默认值 */
    public static void configure(String value) {
        if (value != null && !value.isBlank()) {
            secret = value;
        }
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
            // 恒定时间比较，避免通过响应时间差异猜测签名
            String expected = sign(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
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
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
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
