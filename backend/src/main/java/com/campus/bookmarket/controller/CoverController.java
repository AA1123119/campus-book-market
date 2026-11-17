package com.campus.bookmarket.controller;

import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 本地封面生成服务：根据书名生成确定性配色的 SVG 封面。
 * 作用：离线演示时无需依赖外部图床，评审者拉取代码即可看到完整界面。
 */
@RestController
@RequestMapping("/api/covers")
public class CoverController {

    private static final String[] PALETTES = {
            "#2D5BFF,#6F4EF2", "#0F9D8C,#3ED6A8", "#E6502D,#F5A25D",
            "#7A3FF2,#C86DD7", "#1F7A8C,#57B8C9", "#B03A48,#E4737F",
            "#2F6B3C,#6FBF73", "#4A3AFF,#8A7BFF"
    };

    @GetMapping(value = "/{seed}.svg", produces = "image/svg+xml;charset=UTF-8")
    public String cover(@PathVariable String seed,
                        @RequestParam(name = "t", defaultValue = "") String title) {
        String name = decode(title);
        int idx = Math.abs(seed.hashCode()) % PALETTES.length;
        String[] colors = PALETTES[idx].split(",");
        String safeTitle = escape(name.isEmpty() ? "校园二手书" : name);
        String lines = wrap(safeTitle, 7);

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="300" height="400" viewBox="0 0 300 400">
                  <defs>
                    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%%" stop-color="%s"/>
                      <stop offset="100%%" stop-color="%s"/>
                    </linearGradient>
                  </defs>
                  <rect width="300" height="400" fill="url(#g)"/>
                  <rect x="24" y="24" width="252" height="352" fill="none" stroke="rgba(255,255,255,0.45)" stroke-width="1.5" rx="6"/>
                  <text x="34" y="60" fill="rgba(255,255,255,0.85)" font-size="13"
                        font-family="Microsoft YaHei, PingFang SC, sans-serif">校园二手书 · 书适圈</text>
                  <g fill="#ffffff" font-size="26" font-weight="600"
                     font-family="Microsoft YaHei, PingFang SC, sans-serif">
                %s
                  </g>
                  <text x="34" y="352" fill="rgba(255,255,255,0.8)" font-size="12"
                        font-family="Microsoft YaHei, PingFang SC, sans-serif">CAMPUS BOOK MARKET</text>
                </svg>
                """.formatted(colors[0], colors[1], lines);
    }

    private String wrap(String text, int perLine) {
        StringBuilder sb = new StringBuilder();
        int y = 170;
        for (int i = 0; i < text.length() && i < perLine * 4; i += perLine) {
            String line = text.substring(i, Math.min(text.length(), i + perLine));
            sb.append(String.format("    <text x=\"34\" y=\"%d\">%s</text>%n", y, line));
            y += 38;
        }
        return sb.toString();
    }

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
}
