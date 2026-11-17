package com.campus.bookmarket.service;

import com.campus.bookmarket.dto.BookDtos;
import com.campus.bookmarket.entity.Book;
import com.campus.bookmarket.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 智能定价服务。
 *
 * 设计说明（用于报告 5.3 人机协作案例）：
 * 1) 价格模型 = 原价 × 学科折扣系数 × 成色系数 × 供需修正，输出建议价 + 区间 + 理由；
 * 2) ISBN 识别与文案生成当前由本地知识库 + 模板实现，保证离线可运行、可复现；
 * 3) 接口层与实现分离（AiService），接入真实大模型时只需替换 callLlm()，不影响业务代码。
 */
@Service
@RequiredArgsConstructor
public class AiService {

    private final BookRepository bookRepository;

    /** 学科折扣系数：教材保值率高，考研资料与公共课折价快 */
    private static final Map<String, Double> CATEGORY_FACTOR = new LinkedHashMap<>();

    static {
        CATEGORY_FACTOR.put("数据结构", 0.62);
        CATEGORY_FACTOR.put("计算机网络", 0.60);
        CATEGORY_FACTOR.put("操作系统", 0.60);
        CATEGORY_FACTOR.put("高等数学", 0.55);
        CATEGORY_FACTOR.put("线性代数", 0.55);
        CATEGORY_FACTOR.put("概率论", 0.55);
        CATEGORY_FACTOR.put("大学英语", 0.50);
        CATEGORY_FACTOR.put("大学物理", 0.52);
        CATEGORY_FACTOR.put("考研资料", 0.65);
    }

    public BookDtos.PriceSuggestionResponse suggestPrice(BookDtos.PriceSuggestionRequest req) {
        BigDecimal original = req.getOriginalPrice() == null ? new BigDecimal("49.80") : req.getOriginalPrice();
        int condition = req.getConditionLevel() == null ? 4 : Math.max(1, Math.min(5, req.getConditionLevel()));
        String category = req.getCategory() == null ? "其他" : req.getCategory();

        double categoryFactor = CATEGORY_FACTOR.getOrDefault(category, 0.58);
        // 成色系数：1 成新 0.55 → 5 成新(近乎全新) 0.95
        double conditionFactor = 0.50 + condition * 0.09;

        // 供需修正：同类在售越多，价格越难抬高
        long supply = 0;
        if (req.getIsbn() != null && !req.getIsbn().isBlank()) {
            supply = bookRepository.search(req.getIsbn(), category, null, null, null).size();
        }
        double supplyFactor = supply >= 8 ? 0.92 : supply >= 4 ? 0.96 : 1.0;

        double base = original.doubleValue() * categoryFactor * conditionFactor * supplyFactor;

        // 若平台已有同 ISBN 成交/在售价，取中位数与本模型结果做加权（模拟“市场行情参考”）
        double confidence = 0.72;
        String marketNote = "暂无同款在售记录，按教材通用折旧模型估算";
        if (req.getIsbn() != null && !req.getIsbn().isBlank()) {
            List<Book> same = bookRepository.search(req.getIsbn(), null, null, null, null);
            if (!same.isEmpty()) {
                double avg = same.stream().mapToDouble(b -> b.getPrice().doubleValue()).average().orElse(base);
                base = base * 0.6 + avg * 0.4;
                confidence = Math.min(0.94, 0.78 + same.size() * 0.02);
                marketNote = String.format("平台有 %d 本同款在售，均价 ¥%.2f，已按 40%% 权重参考", same.size(), avg);
            }
        }

        // 成交区间 ±12%，并对下限做兜底（不低于 3 元、不高于原价 9 折）
        double low = Math.max(3.0, base * 0.88);
        double high = Math.min(original.doubleValue() * 0.9, base * 1.12);
        BigDecimal suggested = round(base);
        if (suggested.compareTo(BigDecimal.valueOf(low)) < 0) {
            suggested = round(low);
        }
        if (suggested.compareTo(BigDecimal.valueOf(high)) > 0) {
            suggested = round(high);
        }

        BookDtos.PriceSuggestionResponse res = new BookDtos.PriceSuggestionResponse();
        res.setSuggestedPrice(suggested);
        res.setLowBound(round(low));
        res.setHighBound(round(high));
        res.setConfidence((double) Math.round(confidence * 100) / 100);
        res.setModel("CBM-Pricing-v1 (规则+行情融合)");
        res.setReason(String.format(
                "原价 ¥%.2f × 学科系数 %.2f（%s）× 成色系数 %.2f（%d 成新）× 供需修正 %.2f；%s。"
                        + "建议挂牌 ¥%.2f，可接受区间 ¥%.2f ~ ¥%.2f。",
                original.doubleValue(), categoryFactor, category, conditionFactor, condition,
                supplyFactor, marketNote, suggested.doubleValue(), low, high));
        return res;
    }

    /** ISBN 元数据识别：命中本地教材知识库则直出，否则按 ISBN 前缀推断出版社并给出占位字段 */
    public BookDtos.IsbnResult isbnLookup(String isbn) {
        Map<String, String[]> kb = new LinkedHashMap<>();
        kb.put("9787040498258", new String[]{"数据结构（C语言版）", "严蔚敏", "高等教育出版社", "45.00", "数据结构"});
        kb.put("9787111544937", new String[]{"计算机网络（第7版）", "谢希仁", "机械工业出版社", "49.00", "计算机网络"});
        kb.put("9787111630889", new String[]{"操作系统概念（第9版）", "Silberschatz", "机械工业出版社", "89.00", "操作系统"});
        kb.put("9787040396638", new String[]{"高等数学（上册）第七版", "同济大学数学系", "高等教育出版社", "42.80", "高等数学"});
        kb.put("9787040396645", new String[]{"线性代数（第六版）", "同济大学数学系", "高等教育出版社", "28.60", "线性代数"});
        kb.put("9787513591764", new String[]{"新视野大学英语读写教程1", "郑树棠", "外语教学与研究出版社", "45.90", "大学英语"});

        BookDtos.IsbnResult r = new BookDtos.IsbnResult();
        r.setIsbn(isbn);
        String[] hit = kb.get(isbn);
        if (hit != null) {
            r.setTitle(hit[0]);
            r.setAuthor(hit[1]);
            r.setPublisher(hit[2]);
            r.setOriginalPrice(new BigDecimal(hit[3]));
            r.setCategory(hit[4]);
            r.setCoverUrl(localCover(hit[0]));
            return r;
        }
        r.setPublisher(isbn != null && isbn.startsWith("978704") ? "高等教育出版社" : "未知出版社");
        r.setTitle("未识别书籍（请手动补充书名）");
        r.setOriginalPrice(new BigDecimal("49.80"));
        r.setCategory("其他");
        return r;
    }

    /** 卖点文案生成：依据书名/成色/笔记情况生成 3 句描述，避免用户“一句话挂书” */
    public Map<String, String> generateDescription(Map<String, String> body) {
        String title = body.getOrDefault("title", "这本书");
        String condition = body.getOrDefault("conditionLevel", "4");
        String note = body.getOrDefault("note", "无");
        String desc = String.format(
                "%s，成色约 %s 成新，%s，无缺页无涂改，适合本学期跟课使用。"
                        + "本人已修完该课程，笔记工整可一并赠送；教材版本与本校教学大纲一致。"
                        + "支持校内面交或快递驿站自提，价格可小刀，先到先得。",
                title, condition, "无".equals(note) ? "无划线笔记" : note);
        Map<String, String> res = new LinkedHashMap<>();
        res.put("description", desc);
        res.put("model", "CBM-Copy-v1");
        return res;
    }

    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }

    private String localCover(String title) {
        try {
            return "/api/covers/" + Math.abs(title.hashCode()) + ".svg?t="
                    + java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "/api/covers/1.svg";
        }
    }
}
